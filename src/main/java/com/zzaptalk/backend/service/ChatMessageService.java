package com.zzaptalk.backend.service;

import com.zzaptalk.backend.entity.*;
import com.zzaptalk.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;
    private final FriendBlockService friendBlockService;

    // -------------------------------------------------------------------------
    // 사용자 ID로 User 엔티티 조회 (ChatController에서 사용)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 ID를 찾을 수 없습니다: " + userId));
    }

    // -------------------------------------------------------------------------
    // 클라이언트로부터 메시지를 받아 DB에 저장하고 안 읽은 수 업데이트
    //
    // @param roomId 메시지가 전송된 채팅방 ID
    // @param senderUser 메시지를 보낸 사용자 엔티티
    // @param content 메시지 내용
    // @return DB에 저장된 ChatMessage 엔티티
    // -------------------------------------------------------------------------

    @Transactional
    public ChatMessage saveAndPublishMessage(Long roomId, User senderUser, String content) {

        // 채팅방이 존재하는지 확인(없으면 예외 처리)
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 ID를 찾을 수 없습니다: " + roomId));

        // ChatMessage 엔티티 생성 및 저장
        ChatMessage message = ChatMessage.builder()
                .chatRoom(chatRoom)
                .sender(senderUser)
                .content(content)
                .type(MessageType.TEXT) // 일단 TEXT 메시지라고 가정
                .sentAt(LocalDateTime.now())
                .build();
        message = chatMessageRepository.save(message);

        // -------------------------------------------------------------------------
        // 채팅방 참여자들의 안 읽은 메시지 수 업데이트
        // -------------------------------------------------------------------------

        // 해당 방의 모든 ChatRoomUser 레코드를 가져오기
        List<ChatRoomUser> roomUsers = chatRoomUserRepository.findAllByChatRoom(chatRoom);

        // 메시지 보낸 사람을 제외하고 unreadCount 1 증가
        // (save를 명시적으로 호출할 필요 없이 @Transactional에 의해 자동 반영됨)
        for (ChatRoomUser roomUser : roomUsers) {
            if (!roomUser.getUser().getId().equals(senderUser.getId())) {
                roomUser.incrementUnreadCount();
            }
        }

        // 채팅방 정보 업데이트(마지막 메시지 시간 및 내용)
        chatRoom.setLastMessageTime(message.getSentAt());
        chatRoom.setLastMessageContent(message.getContent());

        return message;

    }

    // -------------------------------------------------------------------------
    // 채팅방 이전 메시지 조회
    // (특정 채팅방에 진입했을 때 이전 메시지 목록 조회)
    //
    // @param roomId 조회할 채팅방 ID
    // @return ChatMessage 목록 (오래된 순)
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ChatMessage> getChatMessages(Long roomId) {

        // 채팅방 유효성 검사
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 ID를 찾을 수 없습니다: " + roomId));

        // 오래된 메시지부터 시간 순서대로(ASC) 조회하여 반환
        return chatMessageRepository.findAllByChatRoomOrderBySentAtAsc(chatRoom);

    }

    // 메시지 전송 전 차단 여부 확인
    // 개인 채팅방인 경우에만 차단 확인
    public void validateMessageSend(ChatRoom chatRoom, User sender, User receiver) {

        // 단체 채팅방은 차단 무시
        if (chatRoom.getType() == ChatRoomType.GROUP) {
            return;
        }

        // 개인 채팅만 차단 검증
        if (friendBlockService.isBlocked(sender, receiver)) {
            throw new IllegalArgumentException("차단한 사용자에게 메시지를 보낼 수 없습니다.");
        }

        if (friendBlockService.isBlockedBy(sender, receiver)) {
            throw new IllegalArgumentException("상대방이 회원님의 메시지를 수신하지 않습니다(차단됨).");
        }
    }

    /**
     * 메시지 전송 전 차단 확인 및 메시지 저장
     * - 개인 채팅방: 차단 여부 확인
     * - 단체 채팅방: 차단 무시
     */
    @Transactional
    public ChatMessage validateAndSendMessage(Long roomId, User sender, String content) {

        // 1. 채팅방 조회
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방 ID를 찾을 수 없습니다: " + roomId));

        // 2. 개인 채팅방인 경우 차단 확인
        if (chatRoom.getType() == ChatRoomType.SINGLE) {
            // 2-1. 상대방 찾기
            User receiver = chatRoomUserRepository.findAllByChatRoom(chatRoom).stream()
                    .map(ChatRoomUser::getUser)
                    .filter(user -> !user.getId().equals(sender.getId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("상대방을 찾을 수 없습니다."));

            // 2-2. 차단 확인
            validateMessageSend(chatRoom, sender, receiver);
        }
        // 3. 단체 채팅방은 차단 무시 (바로 메시지 저장)

        // 4. 메시지 저장 (기존 로직 재사용)
        return saveAndPublishMessage(roomId, sender, content);
    }


}