package com.zzaptalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 친구 프로필 상세 조회 응답 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendProfileDto {

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
}