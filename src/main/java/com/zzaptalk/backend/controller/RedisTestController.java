package com.zzaptalk.backend.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class RedisTestController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/redis-test")
    public String testRedis() {
        try {
            // 1. Redis에 데이터 저장 (60초 TTL)
            redisTemplate.opsForValue()
                    .set("test:key", "hello-redis-cloud", 60, TimeUnit.SECONDS);

            // 2. Redis에서 데이터 읽기
            String value = (String) redisTemplate.opsForValue().get("test:key");

            return "✅ Redis 연결 성공! 저장된 값: " + value;

        } catch (Exception e) {
            return "❌ Redis 연결 실패: " + e.getMessage();
        }
    }
}