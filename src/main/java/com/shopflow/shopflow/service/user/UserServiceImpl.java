package com.shopflow.shopflow.service.user;

import java.util.List;

import org.springframework.stereotype.Service;

import com.shopflow.shopflow.dto.user.UserResponse;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> UserResponse.builder()
                        .id(user.getId())
                        .name(user.getName())
                        .email(user.getEmail())
                        .role(user.getRole())
                        .active(user.isActive())
                        .createdAt(user.getCreatedAt())
                        .build()
                    ).toList();
    }

    @Override
    public void deactivateUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(false);
        userRepository.save(user);
    }


    @Override
    public void activateUser(Long id) {
        UserEntity user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found."));
        user.setActive(true);
        userRepository.save(user);
    }
    
}
