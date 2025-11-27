package com.zzaptalk.backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class GroupChatRoomRequest {

    // 사용자가 설정할 그룹 채팅방 이름
    private String roomName;

    // 방에 초대할 사용자 ID 목록
    // (방장 ID는 포함하지 않음)
    private List<Long> invitedUserIds;

}