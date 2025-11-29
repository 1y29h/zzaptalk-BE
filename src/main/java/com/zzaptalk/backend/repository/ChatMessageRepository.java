package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.ChatMessage;
import com.zzaptalk.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    // ===================================================
    // 회원 탈퇴 쿼리 (배치 작업용)
    // 특정 유저가 보낸 모든 메시지의 발신자를 NULL 로 변경
    // 탈퇴 후 3개월 지난 계정을 완전 삭제할 때 사용
    // FK 제약 조건 에러 방지
    // ====================================================

    @Modifying
    @Query("UPDATE ChatMessage cm SET cm.sender = NULL WHERE cm.sender.id = :userId")
    void updateSenderToNull(@Param("userId") Long userId);

    // 발신자가 NULL인 메시지 조회 (테스트/디버깅용)
    @Query("SELECT cm From ChatMessage cm WHERE cm.sender IS NULL")
    List<ChatMessage> findMessagesWithNullSender();

}