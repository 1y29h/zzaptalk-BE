package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.BlockFriendRequest;
import com.zzaptalk.backend.dto.BlockedFriendListResponseDto;
import com.zzaptalk.backend.dto.UpdateBlockRequest;
import com.zzaptalk.backend.entity.BlockType;
import com.zzaptalk.backend.service.CustomUserDetails;
import com.zzaptalk.backend.service.FriendBlockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/friends/blocks")
@RequiredArgsConstructor
public class FriendBlockController {

    private final FriendBlockService friendBlockService;

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
