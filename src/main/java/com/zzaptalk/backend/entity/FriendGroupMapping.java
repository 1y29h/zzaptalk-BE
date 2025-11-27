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
        name = "friend_group_mapping",
        uniqueConstraints = @UniqueConstraint(columnNames = {"friendship_id", "friend_group_id"})
)
public class FriendGroupMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 친구 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friendship_id", nullable = false)
    private Friendship friendship;

    // 그룹
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_group_id", nullable = false)
    private FriendGroup friendGroup;

    // 그룹에 추가된 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }
}
