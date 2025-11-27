package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.ChatRoom;

// record: DTO/값 객체에 적합
// Lombok 없이도 생성자, Getter, toString 등을 자동 제공
public record ChatRoomCreationResult (

    ChatRoom chatRoom,
    // 새로 생성되었는지(true/false)
    boolean isNew

){}