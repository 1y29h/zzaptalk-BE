package com.zzaptalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 커스텀 그룹별 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendGroupDto {

    // 그룹명
    private String groupName;

    // 해당 그룹에 속한 친구 목록
    private List<FriendSummaryDto> friends;

    // 추가할 필드:
    // 그룹이 독립적인 엔티티이기 때문
    // -> 그룹 수정/삭제 시 groupId로 식별해야함
    private Long groupId;        // 그룹 고유 ID
    private Long userId;         // 그룹 소유자 ID
    private LocalDateTime createdAt;  // 그룹 생성 시각

}