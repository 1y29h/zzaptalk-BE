package com.zzaptalk.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String roomId;   // 채팅방 구분용
    private String sender;   // 보낸 사람
    private String receiver; // 받을 사람
    private String content;  // 메시지 내용
}