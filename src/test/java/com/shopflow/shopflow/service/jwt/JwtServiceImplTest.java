package com.shopflow.shopflow.service.jwt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

class JwtServiceImplTest {

    private JwtServiceImpl jwtService;

    // Valid Base64-encoded 32-byte key (256 bits) required for HMAC-SHA256
    private static final String TEST_SECRET = "c2hvcGZsb3ctc2VjcmV0LWtleS1mb3ItdGVzdGluZy1vbmx5";
    private static final long ACCESS_EXPIRATION = 900000L;    // 15 min
    private static final long REFRESH_EXPIRATION = 604800000L; // 7 days

    @BeforeEach
    void setUp() {
        jwtService = new JwtServiceImpl();
        ReflectionTestUtils.setField(jwtService, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtService, "expiration", ACCESS_EXPIRATION);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", REFRESH_EXPIRATION);
    }

    @Test
    void generateToken_ShouldReturnNonNullToken() {
        String token = jwtService.generateToken("patrick@email.com");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractUserName_ShouldReturnCorrectUsername() {
        String token = jwtService.generateToken("patrick@email.com");

        String username = jwtService.extractUserName(token);

        assertEquals("patrick@email.com", username);
    }

    @Test
    void isTokenValid_WhenTokenMatchesUser_ShouldReturnTrue() {
        String token = jwtService.generateToken("patrick@email.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("patrick@email.com");

        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void isTokenValid_WhenTokenBelongsToDifferentUser_ShouldReturnFalse() {
        String token = jwtService.generateToken("patrick@email.com");
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("other@email.com");

        assertFalse(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void getRemainingExpiration_ShouldReturnPositiveValue() {
        String token = jwtService.generateToken("patrick@email.com");

        long remaining = jwtService.getRemainingExpiration(token);

        assertTrue(remaining > 0);
        assertTrue(remaining <= ACCESS_EXPIRATION);
    }

    @Test
    void generateRefreshToken_ShouldReturnTokenWithLongerExpiration() {
        String refreshToken = jwtService.generateRefreshToken("patrick@email.com");

        assertNotNull(refreshToken);
        String username = jwtService.extractUserName(refreshToken);
        assertEquals("patrick@email.com", username);

        long remaining = jwtService.getRemainingExpiration(refreshToken);
        assertTrue(remaining > ACCESS_EXPIRATION);
    }
}
