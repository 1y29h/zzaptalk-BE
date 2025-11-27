package com.zzaptalk.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFriendToGroupRequest {
    private Long friendshipId;  // 친구 관계 ID
    private Long groupId;       // 추가할 그룹 ID
}
