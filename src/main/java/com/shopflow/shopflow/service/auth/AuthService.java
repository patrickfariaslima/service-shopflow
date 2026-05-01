package com.shopflow.shopflow.service.auth;

import com.shopflow.shopflow.dto.token.AuthResponse;
import com.shopflow.shopflow.dto.user.LoginRequest;
import com.shopflow.shopflow.dto.user.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    void logout(String token, String refreshToken);
    AuthResponse refresh(String refreshToken);
}
