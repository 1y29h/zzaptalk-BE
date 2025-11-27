package com.zzaptalk.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateFriendRequest {

    // 대상이 되는 친구의 User ID (필수)
    @NotNull(message = "친구 ID는 필수입니다.")
    private Long friendUserId;

    // 즐겨찾기 토글 (nullable - 값이 있을 때만 업데이트)
    private Boolean isFavorite;

    // 커스텀 그룹명 변경 (nullable - 값이 있을 때만 업데이트, null이면 그룹 해제)
    private String customGroupName;
}