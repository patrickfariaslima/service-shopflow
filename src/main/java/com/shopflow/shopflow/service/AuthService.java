package com.shopflow.shopflow.service;

import com.shopflow.shopflow.dto.AuthResponse;
import com.shopflow.shopflow.dto.LoginRequest;
import com.shopflow.shopflow.dto.RegisterRequest;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
