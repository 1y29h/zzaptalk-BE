package com.zzaptalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 친구 목록 최종 응답 DTO
 * 4개 섹션으로 구분:
 * 1. 생일인 친구
 * 2. 즐겨찾기 친구
 * 3. 커스텀 그룹
 * 4. 기타 친구 (그룹 없음, ㄱㄴㄷ순)
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendListResponseDto {

    // 1. 생일인 친구 (오늘 기준 ±7일)
    private List<FriendSummaryDto> birthdayFriends;

    // 2. 즐겨찾기 친구
    private List<FriendSummaryDto> favoriteFriends;

    // 3. 커스텀 그룹별 친구
    private List<FriendGroupDto> customGroups;

    // 4. 기타 친구 (즐겨찾기X, 그룹X, ㄱㄴㄷ순 정렬)
    private List<FriendSummaryDto> otherFriends;
}