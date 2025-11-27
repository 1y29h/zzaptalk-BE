package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.*;
import com.zzaptalk.backend.entity.FriendGroup;
import com.zzaptalk.backend.entity.FriendGroupMapping;
import com.zzaptalk.backend.entity.Friendship;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.repository.FriendGroupMappingRepository;
import com.zzaptalk.backend.repository.FriendGroupRepository;
import com.zzaptalk.backend.repository.FriendshipRepository;
import com.zzaptalk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendGroupService {

    // Repository 의존성 추가
    private final UserRepository userRepository;
    private final FriendGroupRepository friendGroupRepository;
    private final FriendGroupMappingRepository friendGroupMappingRepository;
    private final FriendshipRepository friendshipRepository;

    // ============================
    // 8. 그룹 생성 메서드 추가
    // ============================
    // 반환 타입을 FriendGroup -> GroupResponseDto 로 변경
    public GroupResponseDto createGroup(Long userId, String groupName) { // 1. 반환 타입 변경 (그룹 목록 조회 할 때 Dto 새로 만듦)
        // 1. 중복 체크
        if (friendGroupRepository.existsByUserIdAndGroupName(userId, groupName)) {
            throw new IllegalArgumentException("이미 존재하는 그룹명입니다.");
        }

        // 2. 그룹 생성
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        FriendGroup group = FriendGroup.builder()
                .user(user)
                .groupName(groupName)
                .build();

        // 3. 일단 entity로 저장
        FriendGroup savedGroup = friendGroupRepository.save(group);

        // 4. DTO로 변환하여 반환
        return new GroupResponseDto(savedGroup);
    }



    // ================
    // 10. 친구를 그룹에 추가하는 메서드 추가
    // =================================
    // 새 메서드:
    public void addFriendToGroup(Long currentUserId, Long friendshipId, Long groupId) {
        // 1. 중복 체크
        if (friendGroupMappingRepository.existsByFriendshipIdAndFriendGroupId(friendshipId, groupId)) {
            throw new IllegalArgumentException("이미 해당 그룹에 추가된 친구입니다.");
        }

        // 2. Friendship과 FriendGroup 존재 여부 확인
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));

        FriendGroup group = friendGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 2.5. 권한 검증
        // 해당 friendship이 현재 사용자의 것인지 확인
        if (!friendship.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인의 친구만 그룹에 추가할 수 있습니다.");
        }

        // 해당 group이 현재 사용자의 것인지 확인
        if (!group.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인의 그룹에만 친구를 추가할 수 있습니다.");
        }

        // 3. 매핑 생성
        FriendGroupMapping mapping = FriendGroupMapping.builder()
                .friendship(friendship)
                .friendGroup(group)
                .build();

        friendGroupMappingRepository.save(mapping);
    }

    // ===========
    // 11. 그룹에서 친구 제거 메서드 추가
    //     -> 그룹에서만 제거하고 친구관계는 유지해야되서
    // ===================
    // 새 메서드:
    @Transactional
    public void removeFriendFromGroup(Long currentUserId, Long friendshipId, Long groupId) {
        // 1. 존재 여부 및 권한 확인
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계를 찾을 수 없습니다."));

        FriendGroup group = friendGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("그룹을 찾을 수 없습니다."));

        // 2. 권한 검증
        if (!friendship.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인의 친구만 그룹에서 제거할 수 있습니다.");
        }

        if (!group.getUser().getId().equals(currentUserId)) {
            throw new IllegalArgumentException("본인의 그룹에서만 친구를 제거할 수 있습니다.");
        }

        // 3. 삭제 실행
        friendGroupMappingRepository.deleteByFriendshipIdAndFriendGroupId(friendshipId, groupId);
    }

    // ===========
    // 12. 내 그룹 목록 조회
    // ===========
    // 반환 타입을 List<FriendGroup> -> List<GroupRespnseDto>로 변경
    @Transactional(readOnly = true)
    public List<GroupResponseDto> getMyGroups(Long userId) { // 1. 반환 타입 변경
        // 2. 엔티티 리스트 조회
        List<FriendGroup> groups = friendGroupRepository.findByUserId(userId);

        // 3. DTO 리스트로 변환 (Stream 사용)
        return groups.stream()
                .map(GroupResponseDto::new) // .map(group -> new GroupResponseDto(group)) 와 도일
                .collect(Collectors.toList());
    }


    // =========================================================================
    // 13. 그룹 삭제
    // =========================================================================
    /*
     * 그룹을 삭제 (연관된 FriendGroupMapping도 Cascade로 자동 삭제됨)
     */
    @Transactional
    public void deleteGroup(Long userId, Long groupId) {
        // 1. 그룹 존재 여부 확인
        FriendGroup group = friendGroupRepository.findById(groupId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 그룹입니다."));

        // 2. 권한 검증 (본인의 그룹인지 확인)
        if (!group.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 그룹만 삭제할 수 있습니다.");
        }

        // 3. 그룹 삭제 (FriendGroupMapping은 CASCADE로 자동 삭제)
        friendGroupRepository.delete(group);
    }
}
