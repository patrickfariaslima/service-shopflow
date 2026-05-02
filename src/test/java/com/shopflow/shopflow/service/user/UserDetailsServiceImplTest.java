package com.shopflow.shopflow.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.enums.UserRole;
import com.shopflow.shopflow.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_WhenUserExists_ShouldReturnUserDetails() {
        UserEntity user = UserEntity.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .password("encodedPassword")
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();

        when(userRepository.findByEmail("john@email.com")).thenReturn(Optional.of(user));

        UserDetails userDetails = userDetailsService.loadUserByUsername("john@email.com");

        assertNotNull(userDetails);
        assertEquals("john@email.com", userDetails.getUsername());
        assertEquals("encodedPassword", userDetails.getPassword());
        assertEquals(1, userDetails.getAuthorities().size());
        
        verify(userRepository).findByEmail("john@email.com");
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ShouldThrowUsernameNotFoundException() {
        when(userRepository.findByEmail("nonexistent@email.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, 
                () -> userDetailsService.loadUserByUsername("nonexistent@email.com"));

        verify(userRepository).findByEmail("nonexistent@email.com");
    }

    @Test
    void loadUserByUsername_WithAdminRole_ShouldReturnUserDetailsWithAdminRole() {
        UserEntity admin = UserEntity.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@email.com")
                .password("encodedPassword")
                .role(UserRole.ADMIN)
                .active(true)
                .build();

        when(userRepository.findByEmail("admin@email.com")).thenReturn(Optional.of(admin));

        UserDetails userDetails = userDetailsService.loadUserByUsername("admin@email.com");

        assertNotNull(userDetails);
        assertEquals("admin@email.com", userDetails.getUsername());
        assertEquals(1, userDetails.getAuthorities().size());
        
        verify(userRepository).findByEmail("admin@email.com");
    }
}
