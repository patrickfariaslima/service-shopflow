package com.shopflow.shopflow.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.shopflow.shopflow.dto.token.AuthResponse;
import com.shopflow.shopflow.dto.user.LoginRequest;
import com.shopflow.shopflow.dto.user.RegisterRequest;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.jwt.JwtService;
import com.shopflow.shopflow.service.refreshtoken.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private JwtService jwtService;
    @Mock private DenyListService denyListService;
    @Mock private RefreshTokenService refreshTokenService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_WhenEmailIsNew_ShouldReturnBothTokens() {
        RegisterRequest request = new RegisterRequest("Patrick", "patrick@email.com", "senha123");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("hashed");
        when(jwtService.generateToken(request.getEmail())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(request.getEmail())).thenReturn("refresh-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(userRepository).save(any(UserEntity.class));
        verify(refreshTokenService).store("refresh-token", request.getEmail());
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowBusinessException() {
        RegisterRequest request = new RegisterRequest("Patrick", "patrick@email.com", "senha123");
        UserEntity existing = UserEntity.builder().email("patrick@email.com").build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(existing));

        assertThrows(BusinessException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void login_WhenCredentialsAreValid_ShouldReturnBothTokens() {
        LoginRequest request = new LoginRequest("patrick@email.com", "senha123");
        UserEntity user = UserEntity.builder().email("patrick@email.com").build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(request.getEmail())).thenReturn("access-token");
        when(jwtService.generateRefreshToken(request.getEmail())).thenReturn("refresh-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token", response.getAccessToken());
        assertEquals("refresh-token", response.getRefreshToken());
        verify(refreshTokenService).store("refresh-token", request.getEmail());
    }

    @Test
    void logout_ShouldDenylistAccessToken_AndInvalidateRefreshToken() {
        String accessToken = "access-token";
        String refreshToken = "refresh-token";

        when(jwtService.getRemainingExpiration(accessToken)).thenReturn(900000L);

        authService.logout(accessToken, refreshToken);

        verify(denyListService).addToDenyList(accessToken, 900000L);
        verify(refreshTokenService).invalidate(refreshToken);
    }

    @Test
    void logout_WhenAccessTokenAlreadyExpired_ShouldStillInvalidateRefreshToken() {
        String accessToken = "expired-access-token";
        String refreshToken = "refresh-token";

        when(jwtService.getRemainingExpiration(accessToken)).thenReturn(-1L);

        authService.logout(accessToken, refreshToken);

        verify(denyListService, never()).addToDenyList(any(), any(Long.class));
        verify(refreshTokenService).invalidate(refreshToken);
    }

    @Test
    void refresh_WhenTokenIsValid_ShouldReturnNewTokensAndRotate() {
        String oldRefreshToken = "old-refresh-token";

        when(refreshTokenService.getUsername(oldRefreshToken)).thenReturn("patrick@email.com");
        when(jwtService.generateToken("patrick@email.com")).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken("patrick@email.com")).thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh(oldRefreshToken);

        assertNotNull(response);
        assertEquals("new-access-token", response.getAccessToken());
        assertEquals("new-refresh-token", response.getRefreshToken());
        verify(refreshTokenService).invalidate(oldRefreshToken);
        verify(refreshTokenService).store("new-refresh-token", "patrick@email.com");
    }

    @Test
    void refresh_WhenTokenIsInvalid_ShouldThrowBusinessException() {
        when(refreshTokenService.getUsername("invalid-token")).thenReturn(null);

        assertThrows(BusinessException.class, () -> authService.refresh("invalid-token"));
    }
}
