package com.zzaptalk.backend.dto;

import lombok.Builder;
import lombok.Getter;

// tokenType과 expiresIn은 하드코딩된 값일 가능성이 높으므로 필드 추가

@Getter
@Builder
public class AuthResponse {

    // AT
    private String accessToken;

    // RT
    private String refreshToken;

    // 토큰 타입(항상 "Bearer")
    private String tokenType;

    // 토큰 만료 시간(밀리초)
    private Long expiresIn;

    // 사용자 정보
    private Long userId;
    private String nickname;

}