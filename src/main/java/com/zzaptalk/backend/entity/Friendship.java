package com.zzaptalk.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(
        name = "friendship",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"})
)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // -------------------------------------------------------------------------
    // 친구 관계 정의
    // -------------------------------------------------------------------------

    // 친구 목록의 '소유자' (로그인한 '나')
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 소유자가 추가한 '친구'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_id", nullable = false)
    private User friend;

    // -------------------------------------------------------------------------
    // 친구 설정
    // -------------------------------------------------------------------------

    // 즐겨찾기 여부
    @Column(nullable = false)
    @Builder.Default
    private boolean isFavorite = false;

//    // 커스텀 그룹 이름 (nullable, 기본 그룹은 null)
//    // 예: "대학친구", "회사동료" 등
//    @Column
//    private String customGroupName;
    // 추가할 부분:
    @OneToMany(mappedBy = "friendship", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FriendGroupMapping> groupMappings;


    // -------------------------------------------------------------------------
    // 메타 정보
    // -------------------------------------------------------------------------

    // 친구 추가 날짜
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}