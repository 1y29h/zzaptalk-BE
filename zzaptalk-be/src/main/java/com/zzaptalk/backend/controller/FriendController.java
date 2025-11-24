package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.*;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.service.CustomUserDetails;
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
}