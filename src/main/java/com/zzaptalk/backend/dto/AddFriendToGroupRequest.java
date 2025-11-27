package com.zzaptalk.backend.dto;

import lombok.*;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddFriendToGroupRequest {
    private List<Long> friendshipIds;  // 친구 관계 ID + 복수형(배열)
    private Long groupId;       // 추가할 그룹 ID
}
