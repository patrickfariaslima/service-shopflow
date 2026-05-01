package com.shopflow.shopflow.service.refreshtoken;

public interface RefreshTokenService {
    void store(String token, String username);

    String getUsername(String token);

    void invalidate(String token);
}
