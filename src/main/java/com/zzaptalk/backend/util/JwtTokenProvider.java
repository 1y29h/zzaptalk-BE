package com.zzaptalk.backend.util;

import com.zzaptalk.backend.entity.User;
import com.zzaptalk.backend.service.CustomUserDetailsService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    // application-local.yml에서 secret 주입
    @Value("${jwt.secret}")
    private String secretKey;

    // application-local.yml에서 expiration 주입
//    @Value("${jwt.expiration}")
//    private long tokenExpiration;

    @Value("${jwt.access-expiration}")
    private long accessExpiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private Key key;
    private final CustomUserDetailsService userDetailsService;

    // -------------------------------------------------------------------------
    // 초기화: Base64 secretKey -> Key 객체로 변환
    // -------------------------------------------------------------------------

    @PostConstruct
    public void init() {
        // Base64 디코딩
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        // HMAC SHA-256 알고리즘에 맞는 Key 객체 생성
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // -------------------------------------------------------------------------
    // JWT 토큰 생성 메서드
    // -------------------------------------------------------------------------

    // Access Tocken 생성
    public String createAccessToken(User user) {

        // Claims(토큰에 담을 정보) 설정
        String identifier = String.valueOf(user.getId());

        Claims claims = Jwts.claims().setSubject(identifier);
        claims.put("userId", user.getId());
        claims.put("nickname", user.getNickname());
//        claims.put("email", user.getEmail());

        // 만료 시간 설정
        Date now = new Date();
        Date validity = new Date(now.getTime() + accessExpiration);

        // 토큰 빌드 및 서명
        return Jwts.builder()
                .setClaims(claims)                          // 정보 저장
                .setIssuedAt(now)                           // 토큰 발행 시간
                .setExpiration(validity)                    // 토큰 만료 시간
                .signWith(key, SignatureAlgorithm.HS256)    // 서명 알고리즘 및 비밀 키 사용
                .compact();                                 // 토큰 압축
    }

    // Refresh Token 생성
    public String createRefreshToken(User user) {
        String identifier = String.valueOf(user.getId());
        Claims claims = Jwts.claims().setSubject(identifier);
        claims.put("userId", user.getId()); // 재발급시 userId 추출용

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshExpiration);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // -------------------------------------------------------------------------
    // JWT 토큰에서 인증 정보 추출
    // -------------------------------------------------------------------------

    // 토큰에서 Subject 정보 추출 메서드
    public String getPrincipal(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody().getSubject();
    }

    public Authentication getAuthentication(String token) {
        String principal = this.getPrincipal(token);

        // CustomUserDetailsService를 사용하여 UserDetails 로드
        UserDetails userDetails = userDetailsService.loadUserByUsername(principal);

        // Spring Security의 Authentication 객체 반환
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    // -------------------------------------------------------------------------
    // 토큰 유효성 + 만료일자 확인
    // -------------------------------------------------------------------------

    // JwtTokenProvider.java
    public boolean validateToken(String token) {

        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        }

        // JWT secret key 불일치
        catch (io.jsonwebtoken.security.SignatureException e) {
            System.err.println("JWT 검증 실패: 서명이 유효하지 않습니다.");
            return false;
        }

        // 토큰 만료
        catch (io.jsonwebtoken.ExpiredJwtException e) {
            System.err.println("JWT 검증 실패: 토큰이 만료되었습니다.");
            return false;
        }

        // 기타 형식 오류 등
        catch (Exception e) {
            System.err.println("JWT 검증 실패: 기타 오류 (" + e.getMessage() + ")");
            return false;
        }

    }

    // =================================
    // 토큰에서 userId 추출
    // ==================================
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.get("userId", Long.class);
    }

    // ======================
    // 토큰의 남은 만료시간 계산 (밀리초)
    // 로그아웃 시 Redis TTL 설정에 사용
    // ==============================
    public long getRemainingTime(String token) {
        Date expiration = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        long now = System.currentTimeMillis();
        long remaning = expiration.getTime() - now;

        // 음수면 0 반환 (이미 만료된 경우)
        return Math.max(remaning, 0);
    }

}