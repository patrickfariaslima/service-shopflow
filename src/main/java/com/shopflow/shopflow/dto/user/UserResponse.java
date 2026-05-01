package com.shopflow.shopflow.dto.user;

import java.time.LocalDateTime;

import com.shopflow.shopflow.enums.UserRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
