package com.shopflow.shopflow.service.auth;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@ExtendWith(MockitoExtension.class)
class DenyListServiceTest {

    @Mock private StringRedisTemplate redisTemplate;
    @Mock private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private DenyListService denyListService;

    @Test
    void addToDenyList_ShouldStoreTokenWithTTL() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        denyListService.addToDenyList("my-token", 900000L);

        verify(valueOperations).set("denyList:my-token", "true", 900000L, TimeUnit.MILLISECONDS);
    }

    @Test
    void isInDenyList_WhenKeyExists_ShouldReturnTrue() {
        when(redisTemplate.hasKey("denyList:my-token")).thenReturn(Boolean.TRUE);

        assertTrue(denyListService.isInDenyList("my-token"));
    }

    @Test
    void isInDenyList_WhenKeyDoesNotExist_ShouldReturnFalse() {
        when(redisTemplate.hasKey("denyList:my-token")).thenReturn(Boolean.FALSE);

        assertFalse(denyListService.isInDenyList("my-token"));
    }

    @Test
    void isInDenyList_WhenRedisReturnsNull_ShouldReturnFalse() {
        when(redisTemplate.hasKey("denyList:my-token")).thenReturn(null);

        assertFalse(denyListService.isInDenyList("my-token"));
    }
}
