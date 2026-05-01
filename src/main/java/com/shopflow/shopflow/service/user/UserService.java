package com.shopflow.shopflow.service.user;

import java.util.List;

import com.shopflow.shopflow.dto.user.UserResponse;

public interface UserService {
    List<UserResponse> getAllUsers();
    void deactivateUser(Long id);
    void activateUser(Long id);
}
