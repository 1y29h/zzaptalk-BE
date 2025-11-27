package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.ChatMessage;
import com.zzaptalk.backend.entity.MessageType;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    private Long messageId;
    private Long roomId;
    private Long senderId;
    // 클라이언트 화면 표시용 닉네임
    private String senderName;
    private String content;
    private MessageType type;
    private LocalDateTime sentAt;

    public static ChatMessageResponse fromEntity(ChatMessage message, String senderNickname) {
        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderName(senderNickname)
                .content(message.getContent())
                .type(message.getType())
                .sentAt(message.getSentAt())
                .build();
    }

}