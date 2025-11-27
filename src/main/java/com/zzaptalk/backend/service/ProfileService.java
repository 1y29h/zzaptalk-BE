package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.MyProfileResponse;
import com.zzaptalk.backend.dto.UpdateProfileRequest;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProfileService {

    private final UserRepository userRepository;

    // =========================================================================
    // 1. 본인 프로필 조회
    // =========================================================================

    @Transactional(readOnly = true)
    public MyProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return buildMyProfileResponse(user);
    }

    // =========================================================================
    // 2. 본인 프로필 수정
    // =========================================================================

    @Transactional
    public MyProfileResponse updateMyProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // null이 아닌 필드만 업데이트 (부분 업데이트)
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            user.setNickname(request.getNickname());
        }

        if (request.getStatusMessage() != null) {
            user.setStatusMessage(request.getStatusMessage());
        }

        if (request.getProfilePhotoUrl() != null) {
            user.setProfilePhotoUrl(request.getProfilePhotoUrl());
        }

        if (request.getBackgroundPhotoUrl() != null) {
            user.setBackgroundPhotoUrl(request.getBackgroundPhotoUrl());
        }

        // JPA 변경 감지로 자동 저장
        User savedUser = userRepository.save(user);

        return buildMyProfileResponse(savedUser);
    }

    // =========================================================================
    // 3. 내부 헬퍼 메서드
    // =========================================================================

    /**
     * User 엔티티를 MyProfileResponse DTO로 변환
     */
    private MyProfileResponse buildMyProfileResponse(User user) {
        return MyProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .nickname(user.getNickname())
                .zzapID(user.getZzapID())
                .profilePhotoUrl(getProfilePhotoUrlOrDefault(user.getProfilePhotoUrl()))
                .backgroundPhotoUrl(user.getBackgroundPhotoUrl())
                .statusMessage(user.getStatusMessage())
                .birthday(user.getBirthday())
                .build();
    }

    /**
     * 프로필 사진 URL이 없으면 기본 이미지 반환
     */
    private String getProfilePhotoUrlOrDefault(String profilePhotoUrl) {
        if (profilePhotoUrl == null || profilePhotoUrl.isBlank()) {
            return "/api/v1/static/default-profile.png";
        }
        return profilePhotoUrl;
    }
}
