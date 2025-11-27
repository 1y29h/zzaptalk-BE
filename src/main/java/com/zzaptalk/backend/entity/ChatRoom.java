package com.zzaptalk.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {

    // PK: 채팅방 고유 ID
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 채팅방 유형(갠챗/단톡)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChatRoomType type;

    // 채팅방 이름
    @Column(length = 255)
    private String name;

    // 채팅방 생성 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 마지막 메시지 전송 시간
    // (정렬 및 최신 메시지 표시에 사용)
    @Setter
    private LocalDateTime lastMessageTime;

    // 마지막 메시지 내용
    @Setter
    private String lastMessageContent;

    @Builder
    public ChatRoom(ChatRoomType type, String name, LocalDateTime createdAt) {
        this.type = type;
        this.name = name;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.lastMessageTime = this.createdAt;
    }

}