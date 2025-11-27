package com.zzaptalk.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class ChatMessageRequest {

    // 메시지 타입
    // (TEXT, IMAGE, ENTER 등)
    private String type;

    // 메시지를 보낸 사용자 ID
    // (토큰에서 추출하는 것이 정석이나, DTO에 포함한다고 가정)
    private Long senderId;

    // 메시지가 전송된 채팅방 ID
    private Long roomId;

    // 메시지 내용
    private String content;

}