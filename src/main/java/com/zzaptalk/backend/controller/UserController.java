package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.*;
import com.zzaptalk.backend.entity.RefreshToken;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.service.*;
import com.zzaptalk.backend.util.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.zzaptalk.backend.dto.MyProfileResponse;
import com.zzaptalk.backend.dto.UpdateProfileRequest;

import java.time.LocalDateTime;
import java.util.Map;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ProfileService profileService;
    private final RefreshTokenService refreshTokenService;
    private final RedisService redisService;

    // -------------------------------------------------------------------------
    // 회원가입(Sign Up) API 엔드포인트
    // POST /api/v1/users/signup
    // -------------------------------------------------------------------------

    @PostMapping("/signup")
    // @Valid: DTO 유효성 검사(@NotBlank 등) 수행
    public ResponseEntity<String> signUp(@Valid @RequestBody UserSignUpRequest request) {

        try {
            // void 타입의 Service 메서드 호출
            userService.signUp(request);
            // 성공 시 HTTP 201 Created 응답 반환
            return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 성공적으로 완료되었습니다.");
        }

        catch (IllegalArgumentException e) {
            // Service 계층에서 발생시킨 중복 예외 처리(400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        catch (Exception e) {
            // 기타 서버 오류 처리(500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("회원가입 중 서버 오류가 발생했습니다.");
        }

    }

    // -------------------------------------------------------------------------
    // 로그인(Log In) API 엔드포인트
    // POST /api/v1/users/login
    // -------------------------------------------------------------------------

    @PostMapping("/login")
    // @Valid: DTO 유효성 검사(@NotBlank 등) 수행
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {

        try {
            // 1. 사용자 인증
            User loggedInUser = userService.login(request);

            // 2. Access Token 생성 (30분)
            String accessToken = jwtTokenProvider.createAccessToken(loggedInUser);

            // 3. Refresh Token 생성 (14일)
            String refreshToken = jwtTokenProvider.createRefreshToken(loggedInUser);

            // 4. Refresh Token을 해싱하여 RDS에 저장
            LocalDateTime expiryDate = LocalDateTime.now().plusDays(14);
            refreshTokenService.saveRefreshToken(
                    loggedInUser.getId(),
                    refreshToken, // 원본 토크 전달 (Service에서 해싱)
                    expiryDate
            );

            // 5. 응답 생성 (AT, RT 모두 반환)
            AuthResponse response = AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken) // 클라이언트에는 원본 전달
                    .tokenType("Bearer")
                    .expiresIn(1800000L) // 30분
                    .userId(loggedInUser.getId())
                    .nickname(loggedInUser.getNickname())
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그인 중 서버 오류가 발생했습니다.");
        }
    }

    // -------------------------------------------------------------------------
    // 유효성 검사(DTO Validation) 예외 처리기
    // @Valid 어노테이션 실패 시 발생하는 예외를 처리
    // -------------------------------------------------------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(MethodArgumentNotValidException ex) {

        // 여러 필드 오류 중 첫 번째 오류 메시지만 클라이언트에게 반환
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .findFirst() // 첫 번째 에러를 찾음
                .map(error -> {
                    if (error instanceof FieldError fieldError) {
                        return fieldError.getDefaultMessage();
                    }
                    return error.getDefaultMessage();
                })
                .orElse("유효성 검사에 실패했습니다."); // 찾지 못할 경우 기본 메시지

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);

    }

// -------------------------------------------------------------------------
// 본인 프로필 조회 API
// GET /api/v1/users/profile
// -------------------------------------------------------------------------

    @GetMapping("/profile")
    public ResponseEntity<?> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            MyProfileResponse profile = profileService.getMyProfile(
                    userDetails.getUserId()
            );
            return ResponseEntity.ok(profile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 조회 중 오류가 발생했습니다.");
        }
    }

// -------------------------------------------------------------------------
// 본인 프로필 수정 API
// PUT /api/v1/users/profile
// -------------------------------------------------------------------------

    @PutMapping("/profile")
    public ResponseEntity<?> updateMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        try {
            MyProfileResponse updatedProfile = profileService.updateMyProfile(
                    userDetails.getUserId(),
                    request
            ); // 전달받은 업데이트 정보로 User 엔티티 수정 -> 결과를 MyProfileResponse 로 반환
            return ResponseEntity.ok(updatedProfile);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("프로필 수정 중 오류가 발생했습니다.");
        }
    }

// ===============================
// 로그아웃 API
// POST / api/v1/users/logout
// ================================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader ("Authorization") String authHeader,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ){
        try{
            // 1. Authorization 헤더에서 Access Token 추출
            String accessToken = authHeader.substring(7); // "Bearer " 제거

            // 2. Access Token을 Redis 블랙리스트에 등록
            long remainingTime = jwtTokenProvider.getRemainingTime(accessToken);
            if (remainingTime > 0){
                redisService.addToBlacklist(accessToken, remainingTime);
            }

            // 3. Refresh Token 삭제 (RDS)
            refreshTokenService.deleteByUserId(userDetails.getUserId());
            return ResponseEntity.ok("로그아웃이 완료되었습니다.");
        }

        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("로그아웃 중 오류가 발생했습니다.");
        }
    }

    /*
     * Access Token 재발급 API
     * POST /api/v1/users/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody Map<String, String> request) {
        try {
            String refreshToken = request.get("refreshToken");

            // 1. Refresh Token 형식 검증 (JWT 파싱)
            if (!jwtTokenProvider.validateToken(refreshToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 Refresh Token입니다.");
            }

            // 2. RT에서 userId 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(refreshToken);

            // 3. DB에서 해시값 조회 후 matches() 비교 (핵심!)
            RefreshToken validToken = refreshTokenService.validateRefreshToken(refreshToken, userId);

            // 4. User 조회
            User user = userService.findById(validToken.getUserId());

            // 5. 새로운 Access Token 발급
            String newAccessToken = jwtTokenProvider.createAccessToken(user);

            // 6. 응답 (기존 RT 재사용)
            AuthResponse response = AuthResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(refreshToken)  // 기존 RT 유지
                    .tokenType("Bearer")
                    .expiresIn(1800000L)
                    .userId(user.getId())
                    .nickname(user.getNickname())
                    .build();

            return ResponseEntity.ok(response);
        }
        catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("토큰 재발급 중 오류가 발생했습니다.");
        }
    }


    // =========================================================================
    // 회원 탈퇴 API
    // DELETE /api/v1/users/account
    // =========================================================================

    /*
     * 회원 탈퇴 처리
     *
     * 처리 내용:
     * 1. Access Token 블랙리스트 등록
     * 2. Refresh Token 삭제
     * 3. 모든 연관 데이터 정리
     * 4. 개인정보 마스킹
     * 5. 상태를 DELETED로 변경
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(
            @RequestHeader("Authorization") String authHeader,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        try {
            // Authorization 헤더에서 Access Token 추출
            String accessToken = authHeader.substring(7); // "Bearer " 제거

            // 회원 탈퇴 처리
            userService.deleteAccount(userDetails.getUserId(), accessToken);

            return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");

        } catch (IllegalArgumentException e) {
            // 비즈니스 로직 에러 (이미 탈퇴한 계정 등)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());

        } catch (Exception e) {
            // 예상치 못한 서버 에러
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원 탈퇴 중 오류가 발생했습니다.");
        }
    }
}