package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.ChatRoom;
import com.zzaptalk.backend.entity.ChatRoomType;
import com.zzaptalk.backend.entity.ChatRoomUser;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomUserListResponse {

    private Long roomId;
    private ChatRoomType type;
    private String roomName; // 단톡 이름 또는 1:1 상대방 닉네임
    private int unreadCount;
    private LocalDateTime lastMessageTime;
    private String lastMessageContent;

    public static ChatRoomUserListResponse fromEntity(ChatRoomUser roomUser) {
        ChatRoom chatRoom = roomUser.getChatRoom();

        // 1:1 방 이름 로직은 ChatRoomService나 Controller에서 처리하는 것이 일반적이지만,
        // 현재는 ChatRoom의 name이 null이면 기본값 사용
        String displayRoomName = chatRoom.getName() != null ? chatRoom.getName() : "1:1 채팅";

        return ChatRoomUserListResponse.builder()
                .roomId(chatRoom.getId())
                .type(chatRoom.getType())
                .roomName(displayRoomName)
                .unreadCount(roomUser.getUnreadCount())
                .lastMessageTime(chatRoom.getLastMessageTime())
                .lastMessageContent(chatRoom.getLastMessageContent())
                .build();
    }

}