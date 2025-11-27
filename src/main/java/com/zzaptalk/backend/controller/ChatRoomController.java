package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.*;
import com.zzaptalk.backend.entity.ChatRoom;
import com.zzaptalk.backend.service.ChatMessageService;
import com.zzaptalk.backend.service.ChatRoomService;
import com.zzaptalk.backend.service.CustomUserDetails;
import com.zzaptalk.backend.repository.UserRepository;
import com.zzaptalk.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat/rooms")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // 단톡
    // -------------------------------------------------------------------------

    @PostMapping("/group")
    public ResponseEntity<ChatRoomResponse> createGroupChatRoom(
            @RequestBody GroupChatRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // JWT 토큰에서 현재 로그인한 사용자(방장)의 ID를 가져와 User 엔티티 조회
        Long currentUserId = userDetails.getUserId();
        User creatorUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다. ID: " + currentUserId));

        // 서비스를 호출하여 채팅방 생성(메서드명: createGroupRoom)
        ChatRoomResponse response = chatRoomService.createGroupRoom(
                creatorUser,
                request
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // -------------------------------------------------------------------------
    // 갠톡
    // -------------------------------------------------------------------------

    @PostMapping("/single")
    public ResponseEntity<ChatRoomResponse> findOrCreateSingleChatRoom(
            @RequestBody SingleChatRoomRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 현재 로그인된 사용자(User A) 조회
        Long currentUserId = userDetails.getUserId();
        User userA = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("인증된 사용자 정보를 찾을 수 없습니다."));

        // 상대방 사용자(User B) 조회
        Long targetUserId = request.getTargetUserId();
        if (currentUserId.equals(targetUserId)) {
            throw new IllegalArgumentException("자기 자신과 1:1 채팅방을 만들 수 없습니다.");
        }

        User userB = userRepository.findById(targetUserId)
                .orElseThrow(() -> new IllegalArgumentException("상대방 사용자 ID를 찾을 수 없습니다. ID: " + targetUserId));

        // 1:1 채팅방 생성 또는 조회 서비스 호출
        ChatRoomCreationResult result = chatRoomService.findOrCreateSingleChatRoom(userA, userB);
        ChatRoom chatRoom = result.chatRoom();

        List<String> memberNicknames = List.of(userA.getNickname(), userB.getNickname());

        ChatRoomResponse response = ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .memberNicknames(memberNicknames)
                .unreadCount(0)
                .lastMessageTime(chatRoom.getLastMessageTime())
                .lastMessageContent(chatRoom.getLastMessageContent())
                .build();

        if (result.isNew()) {
            // 새로 생성된 경우: 201 Created
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } else {
            // 기존 방을 찾은 경우: 200 OK
            return ResponseEntity.ok(response);
        }

    }

    // 예외 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        // HTTP 400 Bad Request와 함께 서비스에서 던진 오류 메시지를 반환
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    // -------------------------------------------------------------------------
    // 채팅방 목록 조회
    // -------------------------------------------------------------------------

    @GetMapping("/list")
    public ResponseEntity<List<ChatRoomResponse>> getChatRoomList(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 1. 현재 로그인된 사용자 ID 획득
        Long currentUserId = userDetails.getUserId();

        // 2. 서비스 호출: 해당 사용자가 참여하고 있는 모든 채팅방 목록 조회
        List<ChatRoomResponse> chatRoomList = chatRoomService.getChatRoomsByUserId(currentUserId);

        // 3. 200 OK와 함께 목록 반환
        return ResponseEntity.ok(chatRoomList);
    }

    // -------------------------------------------------------------------------
    // 채팅방 이전 메시지 조회
    // -------------------------------------------------------------------------

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<List<ChatMessageResponse>> getChatMessages(
            @PathVariable Long roomId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        // 서비스 호출: 해당 방의 모든 메시지 목록을 오래된 순으로 가져옴
        // (ChatMessageService는 List<ChatMessage>를 반환하고, DTO 변환 과정에서 N+1을 피하기 위해 User 엔티티가 필요)

        // ChatMessageService.getChatMessages(Long roomId)는 List<ChatMessage>를 반환하며,
        // DTO 변환을 위해 Service 계층에서 닉네임을 조회하여 DTO로 변환하여 반환하도록 변경해야 함
        // -> ChatMessageService가 List<ChatMessage>를 반환하고, Stream을 사용하여 DTO로 변환

        List<ChatMessageResponse> messages = chatMessageService.getChatMessages(roomId).stream()
                .map(message -> ChatMessageResponse.fromEntity(message, message.getSender().getNickname()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(messages);

    }

}