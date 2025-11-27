package com.zzaptalk.backend.dto;

import com.zzaptalk.backend.entity.Friendship;
import com.zzaptalk.backend.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 친구 목록에 표시될 친구 요약 정보
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendSummaryDto {

    // 친구의 User ID
    private Long userId;

    // 친구 관계 ID (Friendship 테이블의 PK) -> 나와 친구의 관계를 나타냄
    // ex) 연성 userId = 1, 꿀떡 userId=2 -> 친구관계 ID friendshipId=17
    // 이 값으로 그룹에 추가/제거 작업을 수행
    private Long friendshipId;

    // 닉네임
    private String nickname;

    // 프로필 사진 URL
    private String profilePhotoUrl;

    // 상태 메시지
    private String statusMessage;

    // 생일
    private LocalDate birthday;

    // 즐겨찾기 여부
    private boolean isFavorite;

    // 커스텀 그룹명
    // -> 수정 (단일그룹명(String) -> 여러 그룹 (List<FriendGroupDto)))
    private List<GroupSimpleDto> groups;
    // 이거 그 친구 목록 json응답 받을 때 필요없는 부분 가져올 필요 없어서 GroupSimpleDto 만들어서 반환하는거


    // FriendService 로직 개선
    // N+1 문제 해결 -> 성능 개선!
    public static FriendSummaryDto from(Friendship friendship) {
        User friend = friendship.getFriend();
        return FriendSummaryDto.builder()
                .userId(friend.getId())
                .friendshipId(friendship.getId())
                .nickname(friend.getNickname())
                .profilePhotoUrl(friend.getProfilePhotoUrl())
                .statusMessage(friend.getStatusMessage())
                .birthday(friend.getBirthday())
                .isFavorite(friendship.isFavorite())
                .groups(friendship.getGroupMappings().stream()
                        .map(mapping -> GroupSimpleDto.builder()
                                .groupName(mapping.getFriendGroup().getGroupName())
                                .groupId(mapping.getFriendGroup().getId())
                                .build())
                        .collect(Collectors.toList()))
                .build();
    }
}