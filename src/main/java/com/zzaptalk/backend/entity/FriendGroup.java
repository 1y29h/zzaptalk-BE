package com.zzaptalk.backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "friend_group",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "group_name"})
)
public class FriendGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 그룹을 만든 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 그룹 이름 (예: "대학", "회사")
    @Column(nullable = false)
    private String groupName;

    // 그룹 생성 시각
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // FriendGroupMapping과의 관계
    @OneToMany (mappedBy = "friendGroup", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendGroupMapping> groupMappings = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
