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

    /*
     * ChatMessage 엔티티를 DTO로 변환
     * - 탈퇴한 유저의 경우 "알 수 없는 사용자"로 표시
     * - sender가 NULL인 경우 (3개월 지난 탈퇴 유저) 처리
     */
    public static ChatMessageResponse fromEntity(ChatMessage message, String senderNickname) {
        // sender가 NULL인 경우 (배치 작업으로 완전 삭제된 유저)
        Long senderId = null;
        String displayName = "알 수 없는 사용자";

        if (message.getSender() != null) {
            senderId = message.getSender().getId();
            displayName = senderNickname != null ? senderNickname : "알 수 없는 사용자";
        }

        return ChatMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getChatRoom().getId())
                .senderId(senderId) // NULL 가능
                .senderName(senderNickname)
                .content(message.getContent())
                .type(message.getType())
                .sentAt(message.getSentAt())
                .build();
    }

}