package com.shopflow.shopflow.service.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.shopflow.shopflow.dto.user.UserResponse;
import com.shopflow.shopflow.entity.UserEntity;
import com.shopflow.shopflow.enums.UserRole;
import com.shopflow.shopflow.exception.ResourceNotFoundException;
import com.shopflow.shopflow.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getAllUsers_ShouldReturnMappedList() {
        UserEntity user = UserEntity.builder()
                .id(1L).name("Patrick").email("patrick@email.com")
                .role(UserRole.CUSTOMER).active(true).createdAt(LocalDateTime.now()).build();

        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserResponse> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals("Patrick", result.get(0).getName());
        assertEquals("patrick@email.com", result.get(0).getEmail());
        assertTrue(result.get(0).getActive());
    }

    @Test
    void getAllUsers_WhenNoUsers_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
    }

    @Test
    void deactivateUser_WhenUserExists_ShouldSetActiveToFalse() {
        UserEntity user = UserEntity.builder().id(1L).active(true).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deactivateUser(1L);

        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void deactivateUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deactivateUser(99L));
    }

    @Test
    void activateUser_WhenUserExists_ShouldSetActiveToTrue() {
        UserEntity user = UserEntity.builder().id(1L).active(false).build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.activateUser(1L);

        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void activateUser_WhenUserNotFound_ShouldThrowResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.activateUser(99L));
    }
}
