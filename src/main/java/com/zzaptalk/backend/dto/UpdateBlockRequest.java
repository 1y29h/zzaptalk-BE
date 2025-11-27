package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.BlockType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBlockRequest {

    @NotNull(message = "차단한 친구 ID는 필수입니다.")
    private Long blockedUserId;

    @NotNull(message = "차단 타입은 필수입니다.")
    private BlockType blockType;  // MESSAGE_ONLY, MESSAGE_AND_PROFILE, NONE (차단 해제)
}
