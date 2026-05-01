package com.shopflow.shopflow.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.shopflow.shopflow.dto.token.AuthResponse;
import com.shopflow.shopflow.dto.token.RefreshRequest;
import com.shopflow.shopflow.dto.user.LoginRequest;
import com.shopflow.shopflow.dto.user.RegisterRequest;
import com.shopflow.shopflow.service.auth.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader, @Valid @RequestBody RefreshRequest refreshToken) {
        authService.logout(authHeader.substring(7), refreshToken.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest refreshToken) {
        return ResponseEntity.ok(authService.refresh(refreshToken.getRefreshToken()));
    }
    
    
}
