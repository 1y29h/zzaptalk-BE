package com.zzaptalk.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Access Token을 블랙리스트에 추가
     * @param accessToken 로그아웃할 Access Token
     * @param remainingTime 토큰의 남은 유효 시간 (밀리초)
     */
    public void addToBlacklist(String accessToken, long remainingTime) {
        String key = "blacklist:" + accessToken;
        redisTemplate.opsForValue().set(key, "logout", remainingTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Access Token이 블랙리스트에 있는지 확인
     * @param accessToken 확인할 Access Token
     * @return 블랙리스트에 있으면 true, 없으면 false
     */
    public boolean isBlacklisted(String accessToken) {
        String key = "blacklist:" + accessToken;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}