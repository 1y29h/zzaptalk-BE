package com.zzaptalk.backend.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateGroupRequest {
    private String groupName;  // 생성할 그룹 이름
}
