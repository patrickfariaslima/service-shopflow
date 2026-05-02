package com.shopflow.shopflow.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import com.shopflow.shopflow.dto.user.UserResponse;
import com.shopflow.shopflow.enums.UserRole;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;
import com.shopflow.shopflow.service.user.UserService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        UserResponse user = UserResponse.builder()
                .id(1L)
                .name("John Doe")
                .email("john@email.com")
                .role(UserRole.CUSTOMER)
                .active(true)
                .build();

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/v1/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("John Doe"))
                .andExpect(jsonPath("$[0].email").value("john@email.com"));

        verify(userService).getAllUsers();
    }

    @Test
    void deactivateUser_WithExistingId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).deactivateUser(1L);

        mockMvc.perform(patch("/api/v1/admin/users/1/deactivate"))
                .andExpect(status().isNoContent());

        verify(userService).deactivateUser(1L);
    }

    @Test
    void activateUser_WithExistingId_ShouldReturnNoContent() throws Exception {
        doNothing().when(userService).activateUser(1L);

        mockMvc.perform(patch("/api/v1/admin/users/1/activate"))
                .andExpect(status().isNoContent());

        verify(userService).activateUser(1L);
    }
}
