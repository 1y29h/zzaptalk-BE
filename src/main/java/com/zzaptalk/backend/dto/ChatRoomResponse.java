package com.zzaptalk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ChatRoomResponse {

    private Long roomId;                      // 생성된 채팅방 ID
    private String roomName;                  // 채팅방 이름
    private List<String> memberNicknames;     // 참여 사용자 닉네임 목록
    private int unreadCount;                  // 안 읽은 메시지 수
    private String lastMessageContent;        // 마지막 메시지 내용
    private LocalDateTime lastMessageTime;    // 마지막 메시지 시간

}