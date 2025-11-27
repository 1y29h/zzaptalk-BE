package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.ChatRoom;
import com.zzaptalk.backend.entity.ChatRoomUser;
import com.zzaptalk.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.List;

public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    // 두 사용자 ID로 1:1 채팅방 찾기
    // ChatRoomUser 테이블을 두 번 조인하여 특정 방에 두 사용자가 모두 참여하는 경우의 ChatRoom 찾기
    @Query("SELECT cru1.chatRoom FROM ChatRoomUser cru1 " +
            "JOIN ChatRoomUser cru2 ON cru1.chatRoom = cru2.chatRoom " +
            "WHERE cru1.user.id = :userAId AND cru2.user.id = :userBId " +
            "AND cru1.chatRoom.type = 'SINGLE'")    // 1:1 채팅방만 찾도록 명시
    Optional<ChatRoom> findSingleChatRoomBetweenUsers(@Param("userAId") Long userAId, @Param("userBId") Long userBId);

    // 특정 사용자의 모든 참여 채팅방 조회
    List<ChatRoomUser> findAllByUser(User user);

    // 특정 채팅방의 모든 참여자 정보 가져오기
    List<ChatRoomUser> findAllByChatRoom(ChatRoom chatRoom);

    @Query("SELECT cru FROM ChatRoomUser cru JOIN FETCH cru.chatRoom cr JOIN FETCH cru.user u WHERE cru.user = :user ORDER BY cr.lastMessageTime DESC")
    List<ChatRoomUser> findAllByUserWithChatRoomAndUser(@Param("user") User user);

    @Query("SELECT cru FROM ChatRoomUser cru JOIN FETCH cru.user u WHERE cru.chatRoom.id = :roomId")
    List<ChatRoomUser> findAllByChatRoomIdWithUser(@Param("roomId") Long roomId);

}