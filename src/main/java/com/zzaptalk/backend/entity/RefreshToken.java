package com.zzaptalk.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;  // User ID


    // ==============
    // 해시값을 SHA256 등으로 controller나 dto 측면에서 변환하는 지 확인 필요
    @Column(nullable = false, length = 512)
    private String tokenHash;  // Refresh Token 해시값 (중요: 평문 저장 X)

    @Column(nullable = false)
    private LocalDateTime expiryDate;  // 만료 시간

    @Column(nullable = false)
    private LocalDateTime createdAt;  // 생성 시간
}
