package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.*;
import com.zzaptalk.backend.entity.*;
import com.zzaptalk.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final FriendGroupMappingRepository friendGroupMappingRepository;
    private final FriendBlockRepository friendBlockRepository;  // 사용자 차단 관련

    // =========================================================================
    // 1. 친구 목록 조회 (메인 로직)
    // =========================================================================
    /*
     * 현재 사용자의 친구 목록을 4개 섹션으로 분류하여 반환
     * 1. 생일인 친구 (오늘 기준 ±7일)
     * 2. 즐겨찾기 친구
     * 3. 커스텀 그룹별 친구
     * 4. 기타 친구 (그룹 없음, ㄱㄴㄷ순)
     */

    // === 메인 메서드 ===
    @Transactional(readOnly = true)
    public FriendListResponseDto getFriendList(User currentUser) {
        // 1. 데이터 조회 (Fetch Join)
        List<Friendship> friendships = friendshipRepository.findByUserWithFetchJoin(currentUser);

        // 2. 분류 작업
        FriendListClassifier classifier = new FriendListClassifier(LocalDate.now());
        for (Friendship fs : friendships) {
            FriendSummaryDto dto = FriendSummaryDto.from(fs);
            classifier.classify(dto, fs);
        }

        // 5. 결과 반환
        return classifier.buildResponse();
    }

    // === Private 내부 클래스 ===
    private static class FriendListClassifier {
        private final LocalDate today;
        private final List<FriendSummaryDto> birthdayFriends = new ArrayList<>();
        private final List<FriendSummaryDto> favoriteFriends = new ArrayList<>();
        private final Map<Long, FriendGroupDto> customGroupMap = new HashMap<>();
        private final List<FriendSummaryDto> otherFriends = new ArrayList<>();

        public FriendListClassifier(LocalDate today) {
            this.today = today;
        }

        public void classify(FriendSummaryDto dto, Friendship friendship) {
            if (isBirthdayInRange(friendship.getFriend().getBirthday())) {
                birthdayFriends.add(dto);
            }
            if (friendship.isFavorite()) {
                favoriteFriends.add(dto);
            }
            addToCustomGroups(dto, friendship);
            if (isOtherFriend(friendship)) {
                otherFriends.add(dto);
            }
        }

        private boolean isBirthdayInRange(LocalDate birthday) {
            if (birthday == null) return false;

            // 월-일을 4자리 정수로 변환 (예: 12월 28일 = 1228, 1월 3일 = 103)
            int todayMD = today.getMonthValue() * 100 + today.getDayOfMonth();
            int birthdayMD = birthday.getMonthValue() * 100 + birthday.getDayOfMonth();

            // ±7일 범위 계산
            LocalDate minDate = today.minusDays(7);
            LocalDate maxDate = today.plusDays(7);

            int minMD = minDate.getMonthValue() * 100 + minDate.getDayOfMonth();
            int maxMD = maxDate.getMonthValue() * 100 + maxDate.getDayOfMonth();

            // 연말연초 처리
            if (minMD > maxMD) {
                return birthdayMD >= minMD || birthdayMD <= maxMD;
            } else {
                return birthdayMD >= minMD && birthdayMD <= maxMD;
            }
        }

        private void addToCustomGroups(FriendSummaryDto dto, Friendship friendship) {
            for (FriendGroupMapping mapping : friendship.getGroupMappings()) {
                FriendGroup group = mapping.getFriendGroup();

                FriendGroupDto groupDto = customGroupMap.computeIfAbsent(
                        group.getId(),
                        k -> FriendGroupDto.builder()
                                .groupId(group.getId())
                                .groupName(group.getGroupName())
                                .friends(new ArrayList<>())
                                .build()
                );

                groupDto.getFriends().add(dto);
            }
        }

        private boolean isOtherFriend(Friendship friendship) {
            return !friendship.isFavorite() && friendship.getGroupMappings().isEmpty();
        }

        public FriendListResponseDto buildResponse() {
            otherFriends.sort(Comparator.comparing(FriendSummaryDto::getNickname));
            List<FriendGroupDto> sortedGroups = customGroupMap.values().stream()
                    .sorted(Comparator.comparing(FriendGroupDto::getGroupName))
                    .collect(Collectors.toList());

            return FriendListResponseDto.builder()
                    .birthdayFriends(birthdayFriends)
                    .favoriteFriends(favoriteFriends)
                    .customGroups(sortedGroups)
                    .otherFriends(otherFriends)
                    .build();
        }
    }


    // =========================================================================
    // 2. 친구 추가 (phoneNum 또는 zzapID)
    //     phoneNum 또는 zzapID로 친구를 추가
    //     중복 확인 및 자기 자신 추가 방지 로직 포함
    // =========================================================================
    public void addFriend(User currentUser, AddFriendRequest dto) {

        // 1. 추가할 유저 찾기
        User friendToAdd = null;

        if ("PHONE".equals(dto.getType()) && dto.getIdentifier() != null) {
            friendToAdd = userRepository.findByPhoneNum(dto.getIdentifier())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 전화번호입니다."));

        } else if ("ZZAPID".equals(dto.getType()) && dto.getIdentifier() != null) {
            friendToAdd = userRepository.findByZzapID(dto.getIdentifier())
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 ZzapID입니다."));

        } else {
            throw new IllegalArgumentException("올바르지 않은 요청입니다.");
        }

        // 2. 자기 자신 추가 방지
        if (currentUser.getId().equals(friendToAdd.getId())) {
            throw new IllegalArgumentException("자기 자신을 친구로 추가할 수 없습니다.");
        }

        // 3. 이미 친구인지 확인
        if (friendshipRepository.existsByUserAndFriend(currentUser, friendToAdd)) {
            throw new IllegalArgumentException("이미 추가된 친구입니다.");
        }

        // 4. Friendship 엔티티 생성 및 저장
        Friendship newFriendship = Friendship.builder()
                .user(currentUser)
                .friend(friendToAdd)
                .isFavorite(false)  // 기본값
                .build();

        friendshipRepository.save(newFriendship);
    }

    // =========================================================================
    // 3. 친구 검색 (닉네임 또는 이름)
    // =========================================================================
    @Transactional(readOnly = true)
    public List<FriendSummaryDto> searchFriend(User currentUser, String nicknameQuery) {

        // 1. Repository에서 닉네임으로 1차 필터링
        List<Friendship> friendships = friendshipRepository
                .findByUserAndFriendNicknameContaining(currentUser, nicknameQuery);

        // 2. DTO로 변환하여 반환
        return friendships.stream()
                .map(FriendSummaryDto::from)
                .collect(Collectors.toList());
    }

    // =========================================================================
    // 4. 친구 프로필 조회
    // =========================================================================
    @Transactional(readOnly = true)
    public FriendProfileDto getFriendProfile(User currentUser, Long friendUserId) {

        User friend = userRepository.findById(friendUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if (!friendshipRepository.existsByUserAndFriend(currentUser, friend)) {
            throw new IllegalArgumentException("친구 관계가 아닙니다.");
        }

        // 추가: 상대가 나를 MESSAGE_AND_PROFILE로 차단했는지 확인
        friendBlockRepository.findByUserAndBlockedUser(friend, currentUser)
                .ifPresent(block -> {
                    if (block.getBlockType() == BlockType.MESSAGE_AND_PROFILE) {
                        throw new IllegalArgumentException("프로필을 조회할 수 없습니다.");
                    }
                });

        return FriendProfileDto.builder()
                .userId(friend.getId())
                .name(friend.getName())
                .nickname(friend.getNickname())
                .zzapID(friend.getZzapID())
                .profilePhotoUrl(friend.getProfilePhotoUrl())
                .backgroundPhotoUrl(friend.getBackgroundPhotoUrl())
                .statusMessage(friend.getStatusMessage())
                .birthday(friend.getBirthday())
                .build();
    }


    // =========================================================================
    // 5. 친구 설정 업데이트 (즐겨찾기)
    // =========================================================================
    public void updateFriend(User currentUser, UpdateFriendRequest dto) {

        // 1. 친구 찾기
        User friend = userRepository.findById(dto.getFriendUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. Friendship 조회
        Friendship friendship = friendshipRepository.findByUserAndFriend(currentUser, friend)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계가 아닙니다."));

        // 3. 즐겨찾기 업데이트 (값이 있는 경우에만)
        if (dto.getIsFavorite() != null) {
            friendship.setFavorite(dto.getIsFavorite());
        }

        // 5. 저장 (변경 감지로 자동 업데이트됨)
        friendshipRepository.save(friendship);
    }

    // =========================================================================
    // 6. 주소록 동기화 (자동 친구 추가)
    // -> 사용자 주소록에 있는 전화번호 목록으로 ZZAP TALK 가입자를 자동으로 친구 추가
    // =========================================================================
    public List<FriendSummaryDto> syncContacts(User currentUser, SyncContactsRequest dto) {

        // 1. 주소록 전화번호로 ZZAP TALK 가입자 찾기
        List<User> registeredUsers = userRepository.findByPhoneNumIn(dto.getPhoneNumbers());

        // 2. 추가된 친구 목록 (반환용)
        List<FriendSummaryDto> addedFriends = new ArrayList<>();

        // 3. 각 사용자를 친구로 추가
        for (User friend : registeredUsers) {

            // 3-1. 자기 자신은 제외
            if (currentUser.getId().equals(friend.getId())) {
                continue;
            }

            // 3-2. 이미 친구인 경우 제외
            if (friendshipRepository.existsByUserAndFriend(currentUser, friend)) {
                continue;
            }

            // 3-3. Friendship 생성 및 저장
            Friendship newFriendship = Friendship.builder()
                    .user(currentUser)
                    .friend(friend)
                    .isFavorite(false)
                    .build();

            friendshipRepository.save(newFriendship);

            // 3-4. 추가된 친구 DTO 생성
            FriendSummaryDto friendDto = FriendSummaryDto.from(newFriendship);

            addedFriends.add(friendDto);  // ← 이 줄 추가 필요!

        }  // for문 종료

        return addedFriends;
    }

    // =========================================================================
    // 7. 친구 삭제
    // =========================================================================
    public void deleteFriend(User currentUser, Long friendUserId) {

        // 1. 친구 찾기
        User friend = userRepository.findById(friendUserId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. Friendship 조회 및 삭제
        Friendship friendship = friendshipRepository.findByUserAndFriend(currentUser, friend)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계가 아닙니다."));

        friendshipRepository.delete(friendship);
    }


}