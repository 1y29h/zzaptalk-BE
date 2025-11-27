package com.zzaptalk.backend.jwt;

import com.zzaptalk.backend.util.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // JWT 검증 오류 발생 시 클라이언트에게 올바른 응답을 전달
        try {

            // Request Header에서 JWT 토큰 추출
            String token = resolveToken(request);

            // 토큰 유효성 검사 및 인증 처리
            if (token != null && jwtTokenProvider.validateToken(token)) {
                Authentication authentication = jwtTokenProvider.getAuthentication(token);
                log.info("JWT 인증 성공: 사용자 ID = {}", authentication.getName());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

            // 다음 필터로 요청 전달
            filterChain.doFilter(request, response);

        }

        // 토큰 유효성 검사 등에서 발생한 모든 예외(RuntimeException 포함)
        catch (Exception e) {

            // -------------------------------------------------------------------------
            // CORS 관련 헤더 추가(Preflight 실패 방지 및 CORS 문제 해결)
            // Spring Security의 CorsFilter보다 JwtAuthenticationFilter가 먼저 작동할 때 필요
            // -------------------------------------------------------------------------

            response.setHeader("Access-Control-Allow-Origin", request.getHeader("Origin"));
            response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, DELETE, OPTIONS");
            response.setHeader("Access-Control-Allow-Headers", "*");
            response.setHeader("Access-Control-Allow-Credentials", "true");

            // -------------------------------------------------------------------------
            // 401 Unauthorized 응답 코드 및 메시지 설정
            // -------------------------------------------------------------------------

            // HTTP 401
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"Authentication Failed: " + e.getMessage() + "\"}");

        }

    }

    // Request Header에서 토큰 정보 추출
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

}