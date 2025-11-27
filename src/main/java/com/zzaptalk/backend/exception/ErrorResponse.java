//package com.zzaptalk.backend.exception;
//
//import com.fasterxml.jackson.annotation.JsonInclude;
//import lombok.AllArgsConstructor;
//import lombok.Builder;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//
//import java.time.LocalDateTime;
//import java.util.Map;
//
//@Getter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@JsonInclude(JsonInclude.Include.NON_NULL)
//public class ErrorResponse {
//    private LocalDateTime timestamp;
//    private int status;
//    private String error;
//    private String message;
//    private String path;              //  API 경로
//    private String method;            //  HTTP 메서드
//    private String exceptionType;    // 예외 클래스명
//    private Map<String, String> details;
//    private String debugMessage;      //  개발용 상세 정보
//}
