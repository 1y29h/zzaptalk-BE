package com.zzaptalk.backend.controller;

import com.zzaptalk.backend.dto.AuthResponse;
import com.zzaptalk.backend.dto.UserLoginRequest;
import com.zzaptalk.backend.dto.UserSignUpRequest;
import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.service.UserService;
import com.zzaptalk.backend.util.JwtTokenProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;


@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

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
            // Service 계층에서 사용자 조회 및 비밀번호 검증
            User loggedInUser = userService.login(request);
            // 로그인 성공 시 JWT 토큰 생성
            String jwtToken = jwtTokenProvider.createToken(loggedInUser);

            // 응답 DTO 빌드
            AuthResponse response = AuthResponse.builder()
                    .accessToken(jwtToken)
                    .tokenType("Bearer")
                    // 1시간 (3600000ms)으로 하드코딩 가정
                    .expiresIn(3600000L)
                    // 사용자 정보를 응답에 포함해야 한다면 주석 해제
                    .userId(loggedInUser.getId())
                    .nickname(loggedInUser.getNickname())
                    .build();

            // HTTP 200 OK 응답과 함께 토큰 반환
            return ResponseEntity.ok(response);
        }

        catch (IllegalArgumentException e) {
            // 사용자 정보 불일치(ID/비밀번호 오류) 예외 처리(400 Bad Request)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }

        catch (Exception e) {
            // 기타 서버 오류 처리(500 Internal Server Error)
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 중 서버 오류가 발생했습니다.");
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

}