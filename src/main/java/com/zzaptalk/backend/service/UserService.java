package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.UserLoginRequest;
import com.zzaptalk.backend.dto.UserSignUpRequest;
import com.zzaptalk.backend.entity.*;
import com.zzaptalk.backend.repository.*;
import com.zzaptalk.backend.util.BirthdayUtil;
import com.zzaptalk.backend.util.JwtTokenProvider;
import com.zzaptalk.backend.entity.UserStatus;
import com.zzaptalk.backend.repository.ChatMessageRepository;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 주입

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;
    private final RefreshTokenService refreshTokenService;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final FriendBlockRepository friendBlockRepository;

    private final ChatRoomRepository chatRoomRepository;

    // -------------------------------------------------------------------------
    // 회원가입
    // -------------------------------------------------------------------------

    @Transactional
    public void signUp(UserSignUpRequest request) {

        // 전화번호 중복 검사 (활성 계정)
        if (userRepository.existsByPhoneNum(request.getPhoneNum())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }

        // 비밀번호 단방향 암호화(해싱)
        String encodedPwd = passwordEncoder.encode(request.getPwd());
        // 생일 계산 (추가)
        LocalDate birthday = BirthdayUtil.parseBirthdayFromRrn(request.getRrn());

        // User 엔티티 생성
        User newUser = User.builder()
                .phoneNum(request.getPhoneNum())
                .pwd(encodedPwd)                // 암호화된 비밀번호 저장
                .name(request.getName())
                .nickname(request.getName())    // 닉네임 초기 설정: 본명
                .rrn(request.getRrn())
                .birthday(birthday)  // ← 이 줄 추가
                .email(null)
                .zzapID(null)
                .build();

        userRepository.save(newUser);
    }

    // -------------------------------------------------------------------------
    // 로그인
    // -------------------------------------------------------------------------

    // 로그인 성공 시 User 객체 반환(추후 JWT 토큰 생성에 사용)
    @Transactional(readOnly = true)    // 데이터 변경이 없으므로 readOnly
    public User login(UserLoginRequest request) {

        // 사용자 조회
        Optional<User> userOptional = findUserByIdentifier(request);

        // 사용자 존재 여부 확인
        if (userOptional.isEmpty()) {
            throw new IllegalArgumentException("존재하지 않는 사용자 정보입니다.");
        }

        User user = userOptional.get();

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPwd(), user.getPwd())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 로그인 성공시 User 객체 반환
        return user;

    }

    // UserLoginRequest DTO에 포함된 식별자(핸드폰번호, 이메일, ZzapID)를 찾아 User를 조회하는 내부 메서드
    private Optional<User> findUserByIdentifier(UserLoginRequest request) {

        // 전화번호로 조회
        if (request.getPhoneNum() != null && !request.getPhoneNum().isBlank()) {
            return userRepository.findByPhoneNum(request.getPhoneNum());
        }

        // 이메일로 조회
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return userRepository.findByEmail(request.getEmail());
        }

        // ZzapTalk ID로 조회
        if (request.getZzapID() != null && !request.getZzapID().isBlank()) {
            return userRepository.findByZzapID(request.getZzapID());
        }

        // 모든 식별자가 비어있을 경우(Controller에서 @Valid로 대부분 걸러지지만, 방어 코드)
        throw new IllegalArgumentException("로그인 식별자(전화번호/이메일/ZzapID) 중 하나를 입력해야 합니다.");
    }

    // UserService.java 에 있어야 할 메서드
    @Transactional(readOnly = true)
    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }


    // =========================================================================
    // 회원 탈퇴
    // =========================================================================

    /*
     * 회원 탈퇴 처리 (Soft Delete + 개인정보 마스킹)
     * 처리 순서:
     * 1. Access Token 블랙리스트 등록 (Redis)
     * 2. Refresh Token 삭제 (RDS)
     * 3. 채팅방 참여 정보 삭제
     * 4. 친구 관계 정리 (내가 추가한 친구 + 나를 친구로 가진 사람들)
     * 5. 친구 그룹 삭제
     * 6. 차단 관계 정리
     * 7. 개인정보 마스킹
     * 8. 상태를 DELETED로 변경
     *
     * @param userId 탈퇴할 사용자 ID
     * @param accessToken 현재 사용 중인 Access Token (블랙리스트 등록용)
     */

    @Transactional
    public void deleteAccount(Long userId, String accessToken) {

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 이미 탈퇴한 계정인지 확인
        if (user.getStatus() == UserStatus.DELETED){
            throw new IllegalArgumentException("이미 탈퇴한 계쩡입니다.");
        }

        // 2. Access Token 블랙리스트 등록 (Redis)
        // - 탈퇴 후 남은 AT 유효 시간 동안 재사용 방지
        long remainingTime = jwtTokenProvider.getRemainingTime(accessToken);
        if (remainingTime > 0) {
            redisService.addToBlacklist(accessToken, remainingTime);
        }
        // 3. Refresh Token 삭제 (RDS)
        // - 재발급 차단
        refreshTokenService.deleteByUserId(userId);

        // 4. 채팅방 참여 정보 정리
        // - 모든 채팅방에서 나가기 처리
        // - 1:1 채팅: 상대방에게 "알 수 없는 사용자" 표시됨 (User는 Soft Delete로 남아있음)
        // - 단체 채팅: 참여자 목록에서 제거
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByUser(user);
        chatRoomUserRepository.deleteAll(chatRoomUsers);

        // 5. 친구 관계 정리
        // 5-1. 내가 추가한 친구 목록 삭제
        List<Friendship> myFriendships = friendshipRepository.findByUser(user);
        friendshipRepository.deleteAll(myFriendships);

        // 5-2. 다른 사람의 친구 목록에서 나를 삭제 (중요!)
        // - A, B, C가 나를 친구로 추가한 경우
        // - A, B, C의 친구 목록에서 나를 제거해야 함
        List<Friendship> othersFriendships = friendshipRepository.findByFriend(user);
        friendshipRepository.deleteAll(othersFriendships);

        // 6. 친구 그룹 삭제
        // - cascade = ALL, orphanRemoval = true로 설정되어 있어 FriendGroupMapping도 자동 삭제
        List<FriendGroup> friendGroups = friendGroupRepository.findByUser(user);
        friendGroupRepository.deleteAll(friendGroups);

        // 7. 차단 관계 정리
        // 7-1. 내가 차단한 사용자 목록
        List<FriendBlock> myBlocks = friendBlockRepository.findByUser(user);
        friendBlockRepository.deleteAll(myBlocks);

        // 7-2. 나를 차단한 사용자 목록
        List<FriendBlock> blocksOnMe = friendBlockRepository.findByBlockedUser(user);
        friendBlockRepository.deleteAll(blocksOnMe);

        // 8. 개인정보 마스킹
        // - 개인정보 보호법: 서비스 이용 정보는 즉시 파기
        // - 전화번호, 이메일, 이름, 주민번호 등 삭제
        // - 닉네임은 "알 수 없는 사용자"로 변경 (채팅 메시지 발신자 표시용)
        user.maskPersonalData();

        // 9. 상태 변경 (Soft Delete)
        user.setStatus(UserStatus.DELETED);
        user.setDeletedAt(LocalDateTime.now());

        // 10. 저장 (변경 감지로 자동 업데이트)
        userRepository.save(user);

        // 11. Cloudflare R2 프로필 이미지 삭제 (추후 구현)
        // - 현재는 구현되지 않았지만, 추후 R2 서비스 구현 시 추가
        // if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isEmpty()) {
        //     cloudflareR2Service.deleteFile(user.getProfilePhotoUrl());
        // }
        // if (user.getBackgroundPhotoUrl() != null && !user.getBackgroundPhotoUrl().isEmpty()) {
        //     cloudflareR2Service.deleteFile(user.getBackgroundPhotoUrl());
        // }
    }
}
