package com.shopflow.shopflow.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopflow.shopflow.dto.token.AuthResponse;
import com.shopflow.shopflow.dto.token.RefreshRequest;
import com.shopflow.shopflow.dto.user.LoginRequest;
import com.shopflow.shopflow.dto.user.RegisterRequest;
import com.shopflow.shopflow.service.auth.AuthService;
import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;
import org.springframework.security.core.userdetails.UserDetailsService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private DenyListService denyListService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void register_WithValidRequest_ShouldReturnCreatedAndAuthResponse() throws Exception {
        RegisterRequest request = new RegisterRequest("John Doe", "john@email.com", "Password123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(authService.register(any(RegisterRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).register(any(RegisterRequest.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnOkAndAuthResponse() throws Exception {
        LoginRequest request = new LoginRequest("john@email.com", "Password123");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("access-token")
                .refreshToken("refresh-token")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"));

        verify(authService).login(any(LoginRequest.class));
    }

    @Test
    void logout_WithValidTokens_ShouldReturnNoContent() throws Exception {
        RefreshRequest request = new RefreshRequest("refresh-token");

        doNothing().when(authService).logout(anyString(), anyString());

        mockMvc.perform(post("/api/v1/auth/logout")
                .header("Authorization", "Bearer access-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(authService).logout("access-token", "refresh-token");
    }

    @Test
    void refresh_WithValidRefreshToken_ShouldReturnOkAndNewTokens() throws Exception {
        RefreshRequest request = new RefreshRequest("refresh-token");

        AuthResponse authResponse = AuthResponse.builder()
                .accessToken("new-access-token")
                .refreshToken("new-refresh-token")
                .build();

        when(authService.refresh(anyString())).thenReturn(authResponse);

        mockMvc.perform(post("/api/v1/auth/refresh")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"))
                .andExpect(jsonPath("$.refreshToken").value("new-refresh-token"));

        verify(authService).refresh("refresh-token");
    }
}
