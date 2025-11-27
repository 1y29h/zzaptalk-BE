package com.zzaptalk.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Builder;

@Entity
@Getter
@NoArgsConstructor
@Table(
        // 채팅방 ID와 사용자 ID 조합이 유일해야 함
        // 한 채팅방에 한 사용자는 한 번만 참여 가능
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_chatroom_user",
                        columnNames = {"chatRoomId", "userId"}
                )
        }
)

public class ChatRoomUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // FK: 연결된 채팅방
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    // FK: 연결된 사용자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    // 해당 채팅방에서 읽지 않은 메시지 수
    @Column(nullable = false)
    private int unreadCount;

    @Builder
    public ChatRoomUser(ChatRoom chatRoom, User user, int unreadCount) {
        this.chatRoom = chatRoom;
        this.user = user;
        this.unreadCount = unreadCount;
    }

    // 읽지 않은 메시지 수 증가
    public void incrementUnreadCount() {
        this.unreadCount++;
    }

    // 메시지 읽음 처리(카운트 초기화)
    public void resetUnreadCount() {
        this.unreadCount = 0;
    }

}