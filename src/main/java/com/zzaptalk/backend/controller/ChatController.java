package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.ChatMessageRequest;
import com.zzaptalk.backend.dto.ChatMessageResponse;
import com.zzaptalk.backend.entity.ChatMessage;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    // WebSocket 연결 후 클라이언트가 메시지를 보낼 경로(/app/chat/message)
    @MessageMapping("/chat/message")
    public void sendMessage(ChatMessageRequest request, Principal principal) {

        // Principal에서 인증된 사용자 ID 가져오기
        // JwtChannelInterceptor에서 Authentication 객체의 Name으로 User ID(Long)를 설정했다고 가정
        if (principal == null) {
            // 인증이 안 된 상태이므로, 메시지 처리를 거부하거나 예외를 던질 수 있습니다.
            // (이후 SecurityConfig에서 웹소켓 경로를 authenticated()로 막으면 이 코드는 불필요)
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        Long senderId;
        try {
            // principal.getName()은 String 형태의 사용자 ID입니다.
            senderId = Long.valueOf(principal.getName());
        } catch (NumberFormatException e) {
            throw new SecurityException("유효하지 않은 인증 정보(사용자 ID) 형식입니다.");
        }

        // -------------------------------------------------------------------------

        // ⭐️ User 엔티티 조회 (인증된 ID를 사용)
        // ChatMessageService에 해당 메서드가 있다고 가정합니다.
        User sender = chatMessageService.findUserById(senderId);

        // 메시지 저장 및 DB 업데이트(ChatMessageService 호출)
        // 검증 + 저장 한번에 처리
        // 오류 날 때 -> "saveAndPublishMessage" 로 바꾸기 (원래 검증로직 없던 저장만하는 것)
        ChatMessage savedMessage = chatMessageService.validateAndSendMessage(
                request.getRoomId(),
                sender,
                request.getContent()
        );

        // Response DTO 생성(클라이언트에게 보낼 데이터)
        // sender.getName() 대신 sender.getNickname() 또는 sender.getName() 사용 가정
        ChatMessageResponse response = ChatMessageResponse.fromEntity(savedMessage, sender.getNickname());

        // 메시지를 해당 채팅방을 구독하는 모든 클라이언트에게 전송
        String destination = "/topic/chat/room/" + request.getRoomId();
        messagingTemplate.convertAndSend(destination, response);
    }

}