package com.shopflow.shopflow.service;

import org.springframework.security.core.userdetails.UserDetails;

public interface JwtService {
    String generateToken(String username);

    String extractUserName(String token);

    boolean isTokenValid(String token, UserDetails userDetails);
}
