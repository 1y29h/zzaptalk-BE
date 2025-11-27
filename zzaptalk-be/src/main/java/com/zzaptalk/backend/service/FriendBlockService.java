package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.BlockFriendRequest;
import com.zzaptalk.backend.dto.BlockedFriendDto;
import com.zzaptalk.backend.dto.BlockedFriendListResponseDto;
import com.zzaptalk.backend.dto.UpdateBlockRequest;
import com.zzaptalk.backend.entity.BlockType;
import com.zzaptalk.backend.entity.FriendBlock;
import com.zzaptalk.backend.entity.Friendship;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.repository.FriendBlockRepository;
import com.zzaptalk.backend.repository.FriendshipRepository;
import com.zzaptalk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class FriendBlockService {

    private final FriendBlockRepository friendBlockRepository;
    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    /**
     * 친구 차단
     */
    public void blockFriend(User currentUser, BlockFriendRequest request) {
        // 1. 차단할 사용자 찾기
        User userToBlock = userRepository.findById(request.getFriendUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 자기 자신 차단 방지
        if (currentUser.getId().equals(userToBlock.getId())) {
            throw new IllegalArgumentException("자기 자신을 차단할 수 없습니다.");
        }

        // 3. 친구 관계 확인
        Friendship friendship = friendshipRepository.findByUserAndFriend(currentUser, userToBlock)
                .orElseThrow(() -> new IllegalArgumentException("친구 관계가 아닙니다."));

        // 4. 이미 차단한 경우 확인
        if (friendBlockRepository.existsByUserAndBlockedUser(currentUser, userToBlock)) {
            throw new IllegalArgumentException("이미 차단한 사용자입니다.");
        }

        // 5. 친구 관계 삭제 (차단하면 친구 목록에서 제거)
        friendshipRepository.delete(friendship);

        // 5. FriendBlock 생성 및 저장
        FriendBlock block = FriendBlock.builder()
                .user(currentUser)
                .blockedUser(userToBlock)
                .blockType(request.getBlockType())
                .build();

        friendBlockRepository.save(block);
    }

    /**
     * 차단한 친구 목록 조회
     */
    @Transactional(readOnly = true)
    public BlockedFriendListResponseDto getBlockedFriendList(User currentUser) {
        List<FriendBlock> blocks = friendBlockRepository.findByUser(currentUser);

        List<BlockedFriendDto> blockedFriends = blocks.stream()
                .map(BlockedFriendDto::from)
                .collect(Collectors.toList());

        return BlockedFriendListResponseDto.builder()
                .blockedFriends(blockedFriends)
                .totalCount(blockedFriends.size())
                .build();
    }

    /**
     * 차단 설정 변경 (차단 타입 변경 또는 차단 해제)
     */
    public void updateBlock(User currentUser, UpdateBlockRequest request) {
        // 1. 차단당한 사용자 찾기
        User blockedUser = userRepository.findById(request.getBlockedUserId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 2. 차단 관계 조회
        FriendBlock block = friendBlockRepository.findByUserAndBlockedUser(currentUser, blockedUser)
                .orElseThrow(() -> new IllegalArgumentException("차단하지 않은 사용자입니다."));

        // 3. 차단 해제 (BlockType.NONE)
        if (request.getBlockType() == BlockType.NONE) {
            // 3-1. 차단 해제
            friendBlockRepository.delete(block);
            // 3-2. 친구 관계 복원
            Friendship restoreFriendship = Friendship.builder()
                    .user(currentUser)
                    .friend(blockedUser)
                    .isFavorite(false) // 기본값으로 복원
            .build();

            friendshipRepository.save(restoreFriendship);

        } else {
            // 4. 차단 타입 변경
            block.setBlockType(request.getBlockType());
            friendBlockRepository.save(block);
        }
    }

    /**
     * 특정 사용자를 내가 차단했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isBlocked(User currentUser, User targetUser) {
        return friendBlockRepository.existsByUserAndBlockedUser(currentUser, targetUser);
    }

// ===========================
// 이거 있으면 안될것 같은디 -> FriendService 에서 차피 차단당한 사용자 목록 필터링 아 아닌가 모르겟다
// ===========================

    /**
     * 특정 사용자가 나를 차단했는지 확인
     */
    @Transactional(readOnly = true)
    public boolean isBlockedBy(User currentUser, User targetUser) {
        return friendBlockRepository.existsByUserAndBlockedUser(targetUser, currentUser);
    }

    /**
     * 차단 정보 조회 (메시지 전송 가능 여부 확인용)
     */
    @Transactional(readOnly = true)
    public BlockType getBlockType(User currentUser, User targetUser) {
        return friendBlockRepository.findByUserAndBlockedUser(currentUser, targetUser)
                .map(FriendBlock::getBlockType)
                .orElse(BlockType.NONE);
    }
}
