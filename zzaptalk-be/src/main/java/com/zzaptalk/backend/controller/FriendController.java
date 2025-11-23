package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.*;
import com.zzaptalk.backend.entity.BlockType;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.service.CustomUserDetails;
import com.zzaptalk.backend.service.FriendBlockService;
import com.zzaptalk.backend.service.FriendGroupService;
import com.zzaptalk.backend.service.FriendService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// 추가 (프로필 본인/친구 분리)
import com.zzaptalk.backend.dto.FriendProfileDto;

import java.util.List;

@RestController
@RequestMapping("/api/v1/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;
    private final FriendGroupService friendGroupService;
    private final FriendBlockService friendBlockService;

    // =========================================================================
    // 1. 친구 목록 전체 조회
    // GET /api/v1/friends
    // =========================================================================

    @GetMapping
    public ResponseEntity<FriendListResponseDto> getFriendList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        User currentUser = userDetails.getUser();
        FriendListResponseDto friendList = friendService.getFriendList(currentUser);
        return ResponseEntity.ok(friendList);
    }

    // =========================================================================
    // 2. 친구 추가 (phoneNum / zzapID)
    // POST /api/v1/friends
    // =========================================================================

    @PostMapping
    public ResponseEntity<String> addFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AddFriendRequest request
    ) {
        try {
            friendService.addFriend(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("친구가 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("친구 추가 중 오류가 발생했습니다.");
        }
    }

    // =========================================================================
    // 3. 친구 검색 (닉네임)
    // GET /api/v1/friends/search?nickname=xxx
    // =========================================================================

    @GetMapping("/search")
    public ResponseEntity<List<FriendSummaryDto>> searchFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam("nickname") String nicknameQuery
    ) {
        List<FriendSummaryDto> results = friendService.searchFriend(
                userDetails.getUser(),
                nicknameQuery
        );
        return ResponseEntity.ok(results);
    }

    // =========================================================================
    // 4. 친구 프로필 조회
    // GET /api/v1/friends/{friendId}/profile
    // =========================================================================

    @GetMapping("/{friendId}/profile")
    public ResponseEntity<?> getFriendProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("friendId") Long friendId
    ) {
        try {
            FriendProfileDto profile = friendService.getFriendProfile(
                    userDetails.getUser(),
                    friendId
            );
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =========================================================================
    // 5. 친구 설정 변경 (즐겨찾기, 그룹명)
    // PUT /api/v1/friends
    // =========================================================================

    @PutMapping
    public ResponseEntity<String> updateFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateFriendRequest request
    ) {
        try {
            friendService.updateFriend(userDetails.getUser(), request);
            return ResponseEntity.ok("친구 설정이 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // =========================================================================
    // 6. 주소록 동기화 (자동 친구 추가)
    // POST /api/v1/friends/sync-contacts
    // =========================================================================

    @PostMapping("/sync-contacts")
    public ResponseEntity<?> syncContacts(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody SyncContactsRequest request
    ) {
        try {
            List<FriendSummaryDto> addedFriends = friendService.syncContacts(
                    userDetails.getUser(),
                    request
            );
            return ResponseEntity.ok(addedFriends);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("주소록 동기화 중 오류가 발생했습니다.");
        }
    }

    // =========================================================================
    // 7. 친구 삭제
    // DELETE /api/v1/friends/{friendId}
    // =========================================================================

    @DeleteMapping("/{friendId}")
    public ResponseEntity<String> deleteFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable("friendId") Long friendId
    ) {
        try {
            friendService.deleteFriend(userDetails.getUser(), friendId);
            return ResponseEntity.ok("친구가 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    // ===============
    // 그룹 생성 API 추가
    // -> 사용자가 새 그룹을 만들 수 있어야함
    // ===============
    @PostMapping("/groups")
    public ResponseEntity<?> createGroup(
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
    // 그룹 목록 조회 API
    // -> 사용자가 자신이 만든 그룹 목록을 볼 수 있어야 함
    // ================
    @GetMapping("/groups")
    public ResponseEntity<?> createGroup(
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
    // 친구를 그룹에 추가 API
    // -> 친구를 특정 그룹에 추가하는 기능
    // =============
    @PostMapping("/groups/members")
    public ResponseEntity<?> addFriendToGroup(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody AddFriendToGroupRequest request) {
        try {
            friendGroupService.addFriendToGroup(
                    userDetails.getUser().getId(),
                    request.getFriendshipId(),
                    request.getGroupId());
            return ResponseEntity.ok("그룹에 친구가 추가되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    // =============
    // 그룹에서 친구 제거 API
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
    // 그룹 삭제 API
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

    // =========================================================================
    // 친구 차단
    // POST /api/v1/friends/block
    // =========================================================================
    @PostMapping("/block")
    public ResponseEntity<String> blockFriend(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody BlockFriendRequest request
    ) {
        try {
            friendBlockService.blockFriend(userDetails.getUser(), request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("친구가 차단되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }


    // =========================================================================
// 차단한 친구 목록 조회
// GET /api/v1/friends/block
// =========================================================================
    @GetMapping("/block")
    public ResponseEntity<BlockedFriendListResponseDto> getBlockedFriendList(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        BlockedFriendListResponseDto response =
                friendBlockService.getBlockedFriendList(userDetails.getUser());
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // 차단 설정 변경 (차단 해제 또는 타입 변경)
    // PUT /api/v1/friends/block
    // =========================================================================
    @PutMapping("/block")
    public ResponseEntity<String> updateBlock(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateBlockRequest request
    ) {
        try {
            friendBlockService.updateBlock(userDetails.getUser(), request);

            if (request.getBlockType() == BlockType.NONE) {
                return ResponseEntity.ok("차단이 해제되었습니다.");
            } else {
                return ResponseEntity.ok("차단 설정이 변경되었습니다.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }



}