package com.zzaptalk.backend.service;

import com.zzaptalk.backend.entity.RefreshToken;
import com.zzaptalk.backend.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;  // BCrypt 해싱용

    /**
     * Refresh Token을 해싱하여 DB에 저장
     * @param userId 사용자 ID
     * @param rawToken 원본 Refresh Token (평문)
     * @param expiryDate 만료 시간
     */
    public void saveRefreshToken(Long userId, String rawToken, LocalDateTime expiryDate) {
        // 기존 토큰이 있으면 삭제 (1 사용자 = 1 토큰)
        refreshTokenRepository.findByUserId(userId)
                .ifPresent(refreshTokenRepository::delete);

        // SHA-256으로만 해싱 (BCrypt 제거)
        String hashedToken = hashWithSHA256(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(userId)
                .tokenHash(hashedToken)
                .expiryDate(expiryDate)
                .createdAt(LocalDateTime.now())
                .build();

        refreshTokenRepository.save(refreshToken);
    }


    /**
     * Refresh Token 검증 (userId로 조회 후 matches로 비교)
     * @param rawToken 클라이언트가 보낸 원본 RT (평문)
     * @param userId JWT에서 추출한 사용자 ID
     * @return 검증된 RefreshToken 엔티티
     */
    public RefreshToken validateRefreshToken(String rawToken, Long userId) {
        // 1. userId로 DB에서 해시된 RT 조회
        RefreshToken storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 Refresh Token입니다."));

        // 2. 만료 시간 확인
        if (storedToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new IllegalArgumentException("Refresh Token이 만료되었습니다.");
        }

        // SHA-256 해시 비교 (단순 equals)
        String inputHash = hashWithSHA256(rawToken);
        if (!inputHash.equals(storedToken.getTokenHash())) {
            throw new IllegalArgumentException("Refresh Token이 일치하지 않습니다.");
        }

        return storedToken;
    }

    /**
     * 로그아웃 시 Refresh Token 삭제
     * @param userId 사용자 ID
     */
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }

    private String hashWithSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 해싱 실패", e);
        }
    }
}
