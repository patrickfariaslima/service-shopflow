package com.shopflow.shopflow.security;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import com.shopflow.shopflow.service.auth.DenyListService;
import com.shopflow.shopflow.service.jwt.JwtService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private DenyListService denyListService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthFilter jwtAuthFilter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void doFilterInternal_WhenNoAuthHeader_ShouldContinueFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUserName(anyString());
    }

    @Test
    void doFilterInternal_WhenAuthHeaderNotStartsWithBearer_ShouldContinueFilterChain() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Basic token");

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUserName(anyString());
    }

    @Test
    void doFilterInternal_WhenTokenIsInDenyList_ShouldContinueFilterChain() throws Exception {
        String token = "valid-token";
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(denyListService.isInDenyList(token)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(jwtService, never()).extractUserName(anyString());
    }

    @Test
    void doFilterInternal_WhenValidTokenAndUserNotAuthenticated_ShouldAuthenticateUser() throws Exception {
        String token = "valid-token";
        String username = "john@email.com";
        
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .roles("USER")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(denyListService.isInDenyList(token)).thenReturn(false);
        when(jwtService.extractUserName(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(true);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUserName(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).isTokenValid(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenTokenIsInvalid_ShouldNotAuthenticateUser() throws Exception {
        String token = "invalid-token";
        String username = "john@email.com";
        
        UserDetails userDetails = User.builder()
                .username(username)
                .password("password")
                .roles("USER")
                .build();

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(denyListService.isInDenyList(token)).thenReturn(false);
        when(jwtService.extractUserName(token)).thenReturn(username);
        when(userDetailsService.loadUserByUsername(username)).thenReturn(userDetails);
        when(jwtService.isTokenValid(token, userDetails)).thenReturn(false);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUserName(token);
        verify(userDetailsService).loadUserByUsername(username);
        verify(jwtService).isTokenValid(token, userDetails);
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenUsernameIsNull_ShouldNotLoadUserDetails() throws Exception {
        String token = "valid-token";

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(denyListService.isInDenyList(token)).thenReturn(false);
        when(jwtService.extractUserName(token)).thenReturn(null);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(jwtService).extractUserName(token);
        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void doFilterInternal_WhenUserAlreadyAuthenticated_ShouldNotReloadUserDetails() throws Exception {
        String token = "valid-token";
        String username = "john@email.com";

        Authentication existingAuth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(existingAuth);
        SecurityContextHolder.setContext(securityContext);

        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
        when(denyListService.isInDenyList(token)).thenReturn(false);
        when(jwtService.extractUserName(token)).thenReturn(username);

        jwtAuthFilter.doFilterInternal(request, response, filterChain);

        verify(userDetailsService, never()).loadUserByUsername(anyString());
        verify(filterChain).doFilter(request, response);
    }
}
