package com.shopflow.shopflow.service;

import java.time.LocalDateTime;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopflow.shopflow.dto.AuthResponse;
import com.shopflow.shopflow.dto.LoginRequest;
import com.shopflow.shopflow.dto.RegisterRequest;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.enums.UserRole;
import com.shopflow.shopflow.exception.BusinessException;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent())  {
            throw new BusinessException("Email already registered");
        }

        UserEntity user = UserEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.CUSTOMER)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
        userRepository.save(user);
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder().token(token).build();
    }

    @Override
    public AuthResponse login(LoginRequest request){
        authenticationManager.authenticate((new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())));

        UserEntity user = userRepository.findByEmail(request.getEmail()).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String token = jwtService.generateToken(user.getEmail());
        return AuthResponse.builder().token(token).build();
    }
}
