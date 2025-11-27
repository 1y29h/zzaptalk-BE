package com.zzaptalk.backend.repository;

import com.zzaptalk.backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 현재는 기본 CRUD 기능만 사용

}