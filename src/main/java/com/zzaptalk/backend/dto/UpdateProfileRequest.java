package com.zzaptalk.backend.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import jakarta.validation.constraints.Size;



/**
 * 본인 프로필 수정 요청 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    // 닉네임 (선택)
    @Size (min = 1, max=20, message = "닉네임은 1~20자여야만 합니다.")
    private String nickname;

    // 상태 메시지 (선택)
    @Size(max=500, message = "상태 메시지는 500자까지 가능합니다.")
    private String statusMessage;

    // 프로필 사진 URL (선택)
    //@Pattern(regexp = "^https?://.*", message = "유효한 URL이 아닙니다")
    private String profilePhotoUrl;

    // 배경 사진 URL (선택)
    //@Pattern(regexp = "^https?://.*", message = "유효한 URL이 아닙니다")
    private String backgroundPhotoUrl;
}
