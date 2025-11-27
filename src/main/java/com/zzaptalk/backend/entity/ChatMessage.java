package com.zzaptalk.backend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: 이 메시지가 속한 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roomId", nullable = false)
    private ChatRoom chatRoom;

    // FK: 메시지를 보낸 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "senderId", nullable = false)
    private User sender;

    // 메시지 내용(최대 길이 고려)
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    // 메시지 전송 시간
    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    // 메시지 타입(텍스트, 이미지, 파일 등)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Builder
    public ChatMessage(ChatRoom chatRoom, User sender, String content, MessageType type, LocalDateTime sentAt) {
        this.chatRoom = chatRoom;
        this.sender = sender;
        this.content = content;
        this.type = type;
        this.sentAt = sentAt != null ? sentAt : LocalDateTime.now();
    }

}