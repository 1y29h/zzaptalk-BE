package com.zzaptalk.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SingleChatRoomRequest {

    // 상대방 사용자 ID
    private Long targetUserId;

}