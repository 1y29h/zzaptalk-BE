package com.zzaptalk.backend.service;

import com.zzaptalk.backend.dto.ChatRoomCreationResult;
import com.zzaptalk.backend.dto.ChatRoomResponse;
import com.zzaptalk.backend.dto.GroupChatRoomRequest;
import com.zzaptalk.backend.entity.ChatRoom;
import com.zzaptalk.backend.entity.ChatRoomType;
import com.zzaptalk.backend.entity.ChatRoomUser;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.repository.ChatRoomRepository;
import com.zzaptalk.backend.repository.ChatRoomUserRepository;
import com.zzaptalk.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final UserRepository userRepository;

    // -------------------------------------------------------------------------
    // 1:1 채팅방 생성 또는 조회
    // (사용자가 친구 목록에서 한 명을 선택하고 채팅방 진입을 시도할 때)
    // 이미 1:1 채팅방이 존재하면 그 방을 반환하고, 없으면 새로 생성하여 상대방 닉네임으로 방 이름 설정
    //
    // @param userA 현재 로그인된 사용자
    // @param userB 상대방 사용자
    // @return 생성되거나 조회된 ChatRoom 엔티티
    // -------------------------------------------------------------------------

    @Transactional
    public ChatRoomCreationResult findOrCreateSingleChatRoom(User userA, User userB) {

        // 1. 기존 방을 찾을 경우
        return chatRoomUserRepository.findSingleChatRoomBetweenUsers(userA.getId(), userB.getId())
                .map(chatRoom -> new ChatRoomCreationResult(chatRoom, false))
                .orElseGet(() -> {
                    // 2. 방이 없을 경우: 새로 생성
                    ChatRoom newRoom = createSingleChatRoom(userA, userB);
                    return new ChatRoomCreationResult(newRoom, true);
                });
    }

    // -------------------------------------------------------------------------
    // 1:1 채팅방 생성
    // -------------------------------------------------------------------------

    private ChatRoom createSingleChatRoom(User userA, User userB) {
        LocalDateTime now = LocalDateTime.now();

        // ChatRoom 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .type(ChatRoomType.SINGLE)
                .name(null)
                .createdAt(now)
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);

        // -------------------------------------------------------------------------
        // 두 사용자를 ChatRoomUser 테이블에 추가
        // -------------------------------------------------------------------------

        // 사용자 A (나) 추가
        ChatRoomUser roomUserA = ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(userA)
                .unreadCount(0)
                .build();
        chatRoomUserRepository.save(roomUserA);

        // 사용자 B (상대방) 추가
        ChatRoomUser roomUserB = ChatRoomUser.builder()
                .chatRoom(chatRoom)
                .user(userB)
                .unreadCount(0)
                .build();
        chatRoomUserRepository.save(roomUserB);

        return chatRoom;
    }

    // -------------------------------------------------------------------------
    // 사용자가 현재 참여 중인 모든 채팅방 목록 조회
    // (사용자가 앱을 열고 채팅 탭을 볼 때)
    //
    // 현재 로그인된 사용자가 참여하고 있는 모든 ChatRoomUser 레코드 조회
    // -> 이를 통해 사용자에게 채팅방 목록(ChatRoom)과 각 방의 안 읽은 메시지 수(unreadCount) 제공
    //
    // @param user 현재 로그인된 사용자 엔티티
    // @return 해당 사용자가 참여하고 있는 모든 ChatRoomUser 목록
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ChatRoomUser> findAllUserChatRooms(User user) {
        // ChatRoomUserRepository에 정의된 쿼리 사용
        return chatRoomUserRepository.findAllByUser(user);
    }

    // -------------------------------------------------------------------------
    // 그룹 채팅방 생성
    // 방에 초대할 사용자 목록(DTO)을 받아 새로운 그룹 채팅방을 생성하고,
    // 방장과 초대된 모든 사용자를 참여자로 등록
    //
    // @param creatorUser 방을 만든 사용자(User)
    // @param request 그룹 채팅방 생성 요청 DTO
    // @return 새로 생성된 ChatRoom 엔티티
    // -------------------------------------------------------------------------

    @Transactional
    public ChatRoomResponse createGroupRoom(User creatorUser, GroupChatRoomRequest request) {

        List<Long> requestedUserIds = request.getInvitedUserIds();

        // ChatRoom 엔티티 생성
        ChatRoom chatRoom = ChatRoom.builder()
                .type(ChatRoomType.GROUP)
                .name(request.getRoomName())
                .createdAt(LocalDateTime.now())
                .build();
        chatRoom = chatRoomRepository.save(chatRoom);

        // 참여자 목록: 방장 + 초대된 사용자들
        List<User> allParticipants = new ArrayList<>();
        allParticipants.add(creatorUser);

        // 초대된 사용자 ID 목록(DTO)을 이용해 DB에서 User 엔티티 조회 및 추가
        List<User> invitedUsers = userRepository.findAllById(requestedUserIds);

        // 유효성 검사
        if (requestedUserIds.size() != invitedUsers.size()) {
            // 요청된 ID 개수(예: 2개)와 DB에서 실제 조회된 사용자 개수(예: 1개)가 다르면 오류 발생
            throw new IllegalArgumentException("초대 목록에 존재하지 않는 사용자 ID가 포함되어 있습니다.");
        }

        for (User user : invitedUsers) {
            // 방장이 초대 목록에 포함되어 있을 경우를 대비해 중복 체크(방어코드)
            if (!allParticipants.contains(user)) {
                allParticipants.add(user);
            }
        }

        // 모든 참여자를 ChatRoomUser 테이블에 등록
        for (User user : allParticipants) {
            ChatRoomUser roomUser = ChatRoomUser.builder()
                    .chatRoom(chatRoom)
                    .user(user)
                    .unreadCount(0)
                    .build();
            chatRoomUserRepository.save(roomUser);
        }

        List<String> memberNicknames = allParticipants.stream()
                .map(User::getNickname)
                .collect(java.util.stream.Collectors.toList());

        return ChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .roomName(chatRoom.getName())
                .memberNicknames(memberNicknames)
                // 나머지 필드는 DTO 구조에 맞게 명시적으로 null 또는 기본값 처리
                .unreadCount(0) // 새로 생성된 방이므로 0
                .lastMessageContent(null)
                .lastMessageTime(null)
                .build();

    }

    // -------------------------------------------------------------------------
    // 사용자가 현재 참여 중인 모든 채팅방 목록 조회(Controller에서 사용)
    //
    // @param currentUserId 현재 로그인된 사용자 ID
    // @return ChatRoomResponse 목록
    // -------------------------------------------------------------------------

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getChatRoomsByUserId(Long currentUserId) {

        // 1. 현재 로그인된 사용자 엔티티 조회
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("사용자 ID를 찾을 수 없습니다: " + currentUserId));

        // 2. 해당 사용자가 참여하고 있는 모든 ChatRoomUser 레코드 조회
        // (ChatRoom, User 정보 함께 로딩)
        List<ChatRoomUser> roomUsers = chatRoomUserRepository.findAllByUserWithChatRoomAndUser(currentUser);

        // 3. ChatRoomUser 목록을 ChatRoomResponse DTO 목록으로 변환
        return roomUsers.stream()
                .map(roomUser -> {
                    ChatRoom chatRoom = roomUser.getChatRoom();
                    List<String> memberNicknames = getMemberNicknames(chatRoom.getId());

                    return ChatRoomResponse.builder()
                            .roomId(chatRoom.getId())
                            // 1:1 채팅방의 경우 상대방 닉네임으로 방 이름을 표시
                            .roomName(getDisplayRoomName(chatRoom, memberNicknames, currentUser.getNickname()))
                            .memberNicknames(memberNicknames)
                            .unreadCount(roomUser.getUnreadCount())
                            .lastMessageTime(chatRoom.getLastMessageTime())
                            .lastMessageContent(chatRoom.getLastMessageContent())
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 채팅방 멤버 닉네임 목록 조회
    // -------------------------------------------------------------------------

    private List<String> getMemberNicknames(Long roomId) {
        List<ChatRoomUser> roomUsers = chatRoomUserRepository.findAllByChatRoomIdWithUser(roomId);
        return roomUsers.stream()
                .map(roomUser -> roomUser.getUser().getNickname())
                .collect(java.util.stream.Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // 보조 메서드: 1:1 채팅방 이름 처리 (상대방 닉네임 반환)
    // -------------------------------------------------------------------------

    private String getDisplayRoomName(ChatRoom chatRoom, List<String> memberNicknames, String currentNickname) {

        // 그룹 채팅방은 DB에 저장된 이름 사용
        if (chatRoom.getType() == ChatRoomType.GROUP) {
            return chatRoom.getName();
        }

        // 1:1 채팅방 (SINGLE)은 이름이 null이므로, 상대방의 닉네임을 방 이름으로 사용
        if (chatRoom.getType() == ChatRoomType.SINGLE) {
            String opponentName = memberNicknames.stream()
                    .filter(nickname -> !nickname.equals(currentNickname))
                    .findFirst()
                    .orElse(null);

            // 상대방이 탈퇴한 경우 처리
            // - memberNicknames에 "알 수 없는 사용자"가 포함되어 있음
            // - 또는 ChatRoomUser가 삭제되어 memberNicknames가 비어있을 수 있음
            if (opponentName == null || opponentName.equals("알 수 없는 사용자")) {
                return "알 수 없는 사용자";
            }

            return opponentName;
        }

        return chatRoom.getName();
    }

}