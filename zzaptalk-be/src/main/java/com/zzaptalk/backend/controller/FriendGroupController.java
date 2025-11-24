package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.AddFriendToGroupRequest;
import com.zzaptalk.backend.dto.AddFriendsToGroupResultDto;
import com.zzaptalk.backend.dto.CreateGroupRequest;
import com.zzaptalk.backend.dto.GroupResponseDto;
import com.zzaptalk.backend.service.CustomUserDetails;
import com.zzaptalk.backend.service.FriendGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendGroupController {

    private final FriendGroupService friendGroupService;

    // ===============
    // 그룹 생성
    // -> 사용자가 새 그룹을 만들 수 있어야함
    // ===============
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(  // 메서드명 : createGroup
                                           @RequestBody CreateGroupRequest request,
                                           @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // 반환 타입이 DTO로 변경됨
            GroupResponseDto group = friendGroupService.createGroup(
                    userDetails.getUser().getId(),
                    request.getGroupName()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(group);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // ===========
    // 그룹 목록 조회
    // -> 사용자가 자신이 만든 그룹 목록을 볼 수 있어야 함
    // ================
    @GetMapping("/groups")
    public ResponseEntity<?> createGroup(   // 메서드명 동일
                                            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        try {
            // 반환 타입이 DTO로 변경됨
            List<GroupResponseDto> groups = friendGroupService.getMyGroups(userDetails.getUser().getId());
            return ResponseEntity.ok(groups);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("그룹 목록 조회 중 오류가 발생했습니다.");
        }
    }

    // ========
    // 친구를 그룹에 추가
    // -> 친구를 특정 그룹에 추가하는 기능
    // =============
    @PostMapping("/groups/members")
    public ResponseEntity<?> addFriendsToGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddFriendToGroupRequest request) {
        try {
            // 단일 친구 추가 (하위 호환성 유지) - friendshipIds가 1개인 경우
            if (request.getFriendshipIds() == null || request.getFriendshipIds().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("추가할 친구를 선택해주세요.");
            }

            // 여러 친구 추가
            AddFriendsToGroupResultDto result = friendGroupService.addMultipleFriendsToGroup(
                    userDetails.getUser().getId(),
                    request.getFriendshipIds(),
                    request.getGroupId());

            // 모두 성공한 경우
            if (result.getFailedCount() == 0) {
                return ResponseEntity.ok(result);
            }
            // 일부 실패한 경우 (부분 성공)
            else if (result.getSuccessCount() > 0) {
                return ResponseEntity.status(HttpStatus.MULTI_STATUS).body(result);
            }
            // 모두 실패한 경우
            else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // =============
    // 그룹에서 친구 제거
    // -> 그룹에서만 유지, 친구관계는 유지
    // ==============
    @DeleteMapping("/groups/{groupId}/members/{friendshipId}")
    public ResponseEntity<?> removeFriendFromGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,  // 추가
            @PathVariable Long groupId,
            @PathVariable Long friendshipId
    ) {
        try {
            // userId도 전달 (추가)
            friendGroupService.removeFriendFromGroup(
                    userDetails.getUser().getId(),
                    friendshipId,
                    groupId
            );
            return ResponseEntity.ok("그룹에서 친구가 제거되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }


    // =========================================================================
    // 그룹 삭제
    // -> 그룹을 완전히 삭제 (그룹-친구 매핑도 함께 삭제됨)
    // =========================================================================
    @DeleteMapping("/groups/{groupId}")
    public ResponseEntity<?> deleteGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long groupId
    ) {
        try {
            friendGroupService.deleteGroup(userDetails.getUser().getId(), groupId);
            return ResponseEntity.ok("그룹이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}