package com.shopflow.shopflow.service.auth;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DenyListService {
    private final StringRedisTemplate redisTemplate;

    public void addToDenyList(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                "denyList:" + token,
                "true",
                ttlMillis,
                TimeUnit.MILLISECONDS
        );
    }

    public boolean isInDenyList(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey("denyList:" + token));
    }
}
