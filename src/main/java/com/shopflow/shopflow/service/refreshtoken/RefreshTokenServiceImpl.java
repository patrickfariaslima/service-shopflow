package com.shopflow.shopflow.service.refreshtoken;

import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {
    private final StringRedisTemplate redisTemplate;
    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;


    @Override
    public void store(String token, String username) {
        redisTemplate.opsForValue().set(
            "refreshToken:" + token, 
            username,
            refreshExpiration,
            TimeUnit.MILLISECONDS
        );
    }

    @Override
    public String getUsername(String token) {
        return redisTemplate.opsForValue().get("refreshToken:" + token);
    }


    @Override
    public void invalidate(String token) {
        redisTemplate.delete("refreshToken:" + token);
    }
    
}
