package com.zzaptalk.backend.config;

import com.zzaptalk.backend.jwt.JwtAuthenticationFilter;
import com.zzaptalk.backend.service.RedisService;
import com.zzaptalk.backend.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer; // 추가
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource; // 추가
import org.springframework.web.cors.UrlBasedCorsConfigurationSource; // 추가

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisService redisService;

    // -------------------------------------------------------------------------
    // Http 보안 설정
    // -------------------------------------------------------------------------

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CORS 설정 적용
                .cors(Customizer.withDefaults())

                // JWT 기반 인증이므로 세션 사용 안 함
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CSRF(Cross-Site Request Forgery) 비활성화
                // JWT 사용시 필수
                .csrf(AbstractHttpConfigurer::disable)

                // 폼 로그인, HTTP 기본 인증 비활성화
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)

                // 인가(접근 권한) 설정
                .authorizeHttpRequests(auth -> auth

                        // OPTIONS 요청 무조건 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 회원가입, 로그인, WebSocket 경로는 인증 없이 접근 허용
                        .requestMatchers(
                                "/api/v1/users/signup",
                                "/api/v1/users/login",
                                "/api/v1/users/refresh",
                                "/ws/**",
                                "/redis-test"
                        ).permitAll()

                        .requestMatchers(
                                "/api/chat/rooms/**"    // 채팅방 관련 API
                        ).authenticated()

                        // 그 외 모든 요청은 인증 필요
                        .anyRequest().authenticated()

                )

                // JWT 인증 필터 등록
                // Spring Security 기본 필터인 UsernamePasswordAuthenticationFilter 이전에 실행
                .addFilterBefore(
                        new JwtAuthenticationFilter(jwtTokenProvider,redisService),
                        UsernamePasswordAuthenticationFilter.class
                );

        // ⭐️ 참고: 이전에 있던 길었던 .cors() 블록은 위에 .cors(Customizer.withDefaults())로 대체되었습니다.

        return http.build();
    }

    // -------------------------------------------------------------------------
    // ⭐️ 새로 추가된 CORS 설정 함수 (Bean) ⭐️
    // -------------------------------------------------------------------------

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 1. 접근 허용할 프론트엔드 주소 목록
        config.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",
                "https://zzaptalk.com",
                "https://zzaptalk.pages.dev"
        ));

        // 2. 허용할 HTTP 메서드 (OPTIONS는 필수)
        config.setAllowedMethods(Arrays.asList("POST", "GET", "PUT", "DELETE", "OPTIONS"));

        // 3. 모든 헤더 허용
        config.setAllowedHeaders(Arrays.asList("*"));

        // 4. 인증 정보 (쿠키, JWT 토큰 등) 전송 허용
        config.setAllowCredentials(true);

        // 모든 경로("/**")에 이 설정을 적용
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }


    // -------------------------------------------------------------------------
    // 비밀번호 암호화(해싱)를 위한 PasswordEncoder Bean 등록
    // -------------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt: 단방향 해싱 알고리즘
        return new BCryptPasswordEncoder();
    }

}