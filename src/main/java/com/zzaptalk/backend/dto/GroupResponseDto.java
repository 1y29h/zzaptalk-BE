package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.FriendGroup;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GroupResponseDto {

    private Long groupId;
    private String groupName;

    // FriendGroup 엔티티를 DTO로 변환하는 생성자
    public GroupResponseDto(FriendGroup group) {
        this.groupId = group.getId();
        this.groupName = group.getGroupName();
    }
}