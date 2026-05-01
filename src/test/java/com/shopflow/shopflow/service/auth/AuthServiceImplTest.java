package com.shopflow.shopflow.service.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
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
import com.shopflow.shopflow.dto.user.RegisterRequest;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.repository.UserRepository;
import com.shopflow.shopflow.service.jwt.JwtService;
import com.shopflow.shopflow.service.refreshtoken.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private DenyListService denyListService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

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
}
