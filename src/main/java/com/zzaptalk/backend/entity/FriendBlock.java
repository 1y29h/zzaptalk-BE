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
@Table(
        name = "friend_block",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "blocked_user_id"})
)
public class FriendBlock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 차단한 사람 (나)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 차단당한 사람
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_user_id", nullable = false)
    private User blockedUser;

    // 차단 타입
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private BlockType blockType = BlockType.MESSAGE_ONLY;

    // 차단 날짜
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 차단 설정 수정 날짜
    @Column
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
