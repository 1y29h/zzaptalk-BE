package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.ChatMessage;
import com.zzaptalk.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // -------------------------------------------------------------------------
    // 특정 채팅방의 메시지 목록을 전송 시간 기준 내림차순(최신순)으로 조회
    //
    // @param chatRoom 조회할 채팅방 엔티티
    // @return 메시지 목록(최신순)
    // -------------------------------------------------------------------------
    List<ChatMessage> findAllByChatRoomOrderBySentAtDesc(ChatRoom chatRoom);

    // -------------------------------------------------------------------------
    // 특정 채팅방의 메시지 목록을 전송 시간 기준 오름차순(오래된 순)으로 조회
    //
    // @param chatRoom 조회할 채팅방 엔티티
    // @return 메시지 목록(오래된 순)
    // -------------------------------------------------------------------------
    List<ChatMessage> findAllByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);

}