package com.shopflow.shopflow.service.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService{
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) {
        UserEntity user = userRepository.findByEmail(username)
                                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();

    }
}
