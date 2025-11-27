package com.zzaptalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedFriendListResponseDto {

    private List<BlockedFriendDto> blockedFriends;
    private int totalCount;
}
