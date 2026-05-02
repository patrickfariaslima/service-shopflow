package com.shopflow.shopflow.service.refreshtoken;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceImplTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private RefreshTokenServiceImpl refreshTokenService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshExpiration", 604800000L);
    }

    @Test
    void store_ShouldSaveUsernameWithTTL() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        refreshTokenService.store("my-refresh-token", "patrick@email.com");

        verify(valueOperations).set(
                "refreshToken:my-refresh-token",
                "patrick@email.com",
                604800000L,
                TimeUnit.MILLISECONDS);
    }

    @Test
    void getUsername_WhenTokenExists_ShouldReturnUsername() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refreshToken:my-refresh-token")).thenReturn("patrick@email.com");

        String result = refreshTokenService.getUsername("my-refresh-token");

        assertEquals("patrick@email.com", result);
    }

    @Test
    void getUsername_WhenTokenDoesNotExist_ShouldReturnNull() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("refreshToken:invalid-token")).thenReturn(null);

        String result = refreshTokenService.getUsername("invalid-token");

        assertNull(result);
    }

    @Test
    void invalidate_ShouldDeleteKey() {
        refreshTokenService.invalidate("my-refresh-token");

        verify(redisTemplate).delete("refreshToken:my-refresh-token");
    }
}
