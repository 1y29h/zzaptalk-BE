package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.BlockType;
import com.zzaptalk.backend.entity.FriendBlock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedFriendDto {

    private Long blockId;
    private Long userId;
    private String nickname;
    private String profilePhotoUrl;
    private String statusMessage;
    private BlockType blockType;
    private LocalDateTime blockedAt;

    public static BlockedFriendDto from(FriendBlock block) {
        return BlockedFriendDto.builder()
                .blockId(block.getId())
                .userId(block.getBlockedUser().getId())
                .nickname(block.getBlockedUser().getNickname())
                .profilePhotoUrl(block.getBlockedUser().getProfilePhotoUrl())
                .statusMessage(block.getBlockedUser().getStatusMessage())
                .blockType(block.getBlockType())
                .blockedAt(block.getCreatedAt())
                .build();
    }
}
