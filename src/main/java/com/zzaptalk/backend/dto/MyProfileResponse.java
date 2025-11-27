package com.zzaptalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/*
 * 본인 프로필 조회/수정 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyProfileResponse {

    // 사용자 ID
    private Long userId;

    // 본명
    private String name;

    // 닉네임
    private String nickname;

    // ZzapTalk ID
    private String zzapID;

    // 프로필 사진 URL
    private String profilePhotoUrl;

    // 배경 사진 URL
    private String backgroundPhotoUrl;

    // 상태 메시지
    private String statusMessage;

    // 생일
    private LocalDate birthday;

    // === 나중에 추가 가능한 필드들 (본인만 볼 수 있는 정보) ===
    // private String email;           // 이메일
    // private String phoneNum;        // 전화번호 (마스킹 안함)
    // private LocalDateTime createdAt; // 가입일
}
