package com.zzaptalk.backend.dto;

import lombok.Builder;
import lombok.Getter;

/**
 * 친구가 속한 그룹의 간단한 정보 (꼬리표용 DTO)
 */
@Getter
@Builder
public class GroupSimpleDto {
    private Long groupId;
    private String groupName;
}