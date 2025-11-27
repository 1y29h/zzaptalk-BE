package com.zzaptalk.backend.jwt;

import com.zzaptalk.backend.service.CustomUserDetailsService;
import com.zzaptalk.backend.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;

    // 클라이언트에서 서버로 메시지가 전송되기 전 처리
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {

        // 1. STOMP 헤더 정보 추출
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 2. CONNECT 또는 SEND 명령일 때만 인증 로직 수행
        if (StompCommand.CONNECT.equals(accessor.getCommand()) ||
                StompCommand.SEND.equals(accessor.getCommand())) {

            // 3. Authorization 헤더에서 JWT 추출 (HTTP 헤더가 아닌 STOMP 헤더에서 추출)
            String authorizationHeader = accessor.getFirstNativeHeader("Authorization");

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                String token = authorizationHeader.substring(7); // "Bearer " 제거

                // 4. JWT 토큰 유효성 검증
                if (jwtTokenProvider.validateToken(token)) {

                    // 5. JWT에서 사용자 ID(또는 인증 정보) 추출
                    Authentication authentication = jwtTokenProvider.getAuthentication(token);

                    // 6. STOMP 세션에 인증 정보 저장
                    accessor.setUser(authentication);
                    log.info("WebSocket 인증 성공: User ID = {}", authentication.getName());

                } else {
                    log.warn("WebSocket 인증 실패: 유효하지 않은 JWT 토큰");
                    // 🚨 인증 실패 시 연결 거부 로직 (선택적)
                    // 간단히는 인증 정보를 세팅하지 않아 뒤에서 권한 오류가 나게 할 수 있음
                }
            } else {
                log.warn("WebSocket 인증 실패: Authorization 헤더 없음 또는 형식 오류");
            }
        }

        return message;
    }
}