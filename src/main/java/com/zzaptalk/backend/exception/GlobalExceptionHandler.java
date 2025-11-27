//package com.zzaptalk.backend.exception;
//
//import jakarta.servlet.http.HttpServletRequest;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.AccessDeniedException;
//import org.springframework.validation.FieldError;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.HashMap;
//import java.util.Map;
//
//@Slf4j
//@RestControllerAdvice
//public class GlobalExceptionHandler {
//
//    @Value("${spring.profiles.active:dev}")
//    private String activeProfile;
//
//    /**
//     * 개발 환경인지 확인
//     */
//    private boolean isDevelopment() {
//        return "dev".equals(activeProfile) || "local".equals(activeProfile);
//    }
//
//    /**
//     * 400 Bad Request - 유효성 검증 실패 (@Valid 어노테이션)
//     */
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<ErrorResponse> handleValidationExceptions(
//            MethodArgumentNotValidException ex,
//            HttpServletRequest request
//    ) {
//        Map<String, String> errors = new HashMap<>();
//        ex.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        ErrorResponse response = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.BAD_REQUEST.value())
//                .error("Validation Failed")
//                .message("입력값이 유효하지 않습니다.")
//                .path(request.getRequestURI())
//                .method(request.getMethod())
//                .exceptionType(ex.getClass().getSimpleName())
//                .details(errors)
//                .debugMessage(isDevelopment() ? ex.getMessage() : null)
//                .build();
//
//        log.warn("[Validation Error] {} {} - Fields: {}",
//                request.getMethod(),
//                request.getRequestURI(),
//                errors);
//
//        return ResponseEntity.badRequest().body(response);
//    }
//
//    /**
//     * 400 Bad Request - 잘못된 요청 (IllegalArgumentException)
//     */
//    @ExceptionHandler(IllegalArgumentException.class)
//    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
//            IllegalArgumentException ex,
//            HttpServletRequest request
//    ) {
//        ErrorResponse response = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.BAD_REQUEST.value())
//                .error("Bad Request")
//                .message(ex.getMessage())
//                .path(request.getRequestURI())
//                .method(request.getMethod())
//                .exceptionType(ex.getClass().getSimpleName())
//                .debugMessage(isDevelopment() ? getCallerInfo(ex) : null)
//                .build();
//
//        // 로그 출력
//        log.warn("[Bad Request] {} {} - Message: {}",
//                request.getMethod(),
//                request.getRequestURI(),
//                ex.getMessage());
//
//        // 개발 환경에서만 스택 추적 출력
//        if (isDevelopment()) {
//            log.debug("Stack trace:", ex);
//        }
//
//        return ResponseEntity.badRequest().body(response);
//    }
//
//    /**
//     * 403 Forbidden - 권한 없음
//     */
//    @ExceptionHandler(AccessDeniedException.class)
//    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
//            AccessDeniedException ex,
//            HttpServletRequest request
//    ) {
//        ErrorResponse response = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.FORBIDDEN.value())
//                .error("Forbidden")
//                .message("해당 리소스에 접근할 권한이 없습니다.")
//                .path(request.getRequestURI())
//                .method(request.getMethod())
//                .exceptionType(ex.getClass().getSimpleName())
//                .build();
//
//        log.warn("[Forbidden] {} {} - User attempted unauthorized access",
//                request.getMethod(),
//                request.getRequestURI());
//
//        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
//    }
//
//    /**
//     * 500 Internal Server Error - 예상치 못한 서버 오류
//     */
//    @ExceptionHandler(Exception.class)
//    public ResponseEntity<ErrorResponse> handleGeneralException(
//            Exception ex,
//            HttpServletRequest request
//    ) {
//        ErrorResponse response = ErrorResponse.builder()
//                .timestamp(LocalDateTime.now())
//                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
//                .error("Internal Server Error")
//                .message("서버 내부 오류가 발생했습니다.")
//                .path(request.getRequestURI())
//                .method(request.getMethod())
//                .exceptionType(ex.getClass().getSimpleName())
//                .debugMessage(isDevelopment() ? ex.getMessage() : null)
//                .build();
//
//        // 심각한 오류이므로 항상 전체 스택 추적 로그
//        log.error("[Internal Error] {} {} - Exception: {}",
//                request.getMethod(),
//                request.getRequestURI(),
//                ex.getMessage(),
//                ex);
//
//        return ResponseEntity
//                .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                .body(response);
//    }
//
//    /**
//     * 예외가 발생한 위치 정보 추출
//     */
//    private String getCallerInfo(Exception ex) {
//        if (ex.getStackTrace().length > 0) {
//            StackTraceElement element = ex.getStackTrace()[0];
//            return String.format("at %s.%s (Line: %d)",
//                    element.getClassName(),
//                    element.getMethodName(),
//                    element.getLineNumber());
//        }
//        return "Unknown location";
//    }
//}
