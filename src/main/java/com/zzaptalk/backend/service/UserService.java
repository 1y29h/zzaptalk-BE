package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.UserLoginRequest;
import com.zzaptalk.backend.dto.UserSignUpRequest;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.repository.UserRepository;
import com.zzaptalk.backend.util.BirthdayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // SecurityConfig에서 주입

    // -------------------------------------------------------------------------
    // 회원가입
    // -------------------------------------------------------------------------

    @Transactional
    public void signUp(UserSignUpRequest request) {

        // 전화번호 중복 검사
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
}