package com.shopflow.shopflow.service.auth;

import com.shopflow.shopflow.dto.user.AuthResponse;
import com.shopflow.shopflow.dto.user.LoginRequest;
import com.shopflow.shopflow.dto.user.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
