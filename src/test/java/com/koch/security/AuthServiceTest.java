package com.koch.security;

import com.koch.security.model.AuthRequest;
import com.koch.security.model.AuthResponse;
import com.koch.security.model.Role;
import com.koch.security.model.UserRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private CustomUserDetailsService userDetailsService;
    @Mock
    private JdbcUserRepository userRepository;
    @Mock
    private ResetTokenRepository resetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private AuthService authService;

    private UserDetails mockUserDetails;

    @BeforeEach
    void setUp() {
        mockUserDetails = User.withUsername("testuser")
                .password("testpass")
                .roles("OPERATOR")
                .build();
    }

    @Test
    void testLogin_Success_ReturnsTokens() {
        AuthRequest request = new AuthRequest("testuser", "testpass");
        Authentication authParams = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        Authentication authResult = mock(Authentication.class);
        
        when(authenticationManager.authenticate(authParams)).thenReturn(authResult);
        when(authResult.getPrincipal()).thenReturn(mockUserDetails);
        when(jwtService.generateToken(eq(mockUserDetails), anyLong()))
                .thenReturn("access-token-123")
                .thenReturn("refresh-token-456");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("access-token-123", response.accessToken());
        assertEquals("refresh-token-456", response.refreshToken());
        verify(auditService, never()).logFailure(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void testLogin_Failure_LogsAndThrows() {
        AuthRequest request = new AuthRequest("baduser", "badpass");
        Authentication authParams = new UsernamePasswordAuthenticationToken(request.username(), request.password());
        org.springframework.security.core.AuthenticationException exception = new org.springframework.security.authentication.BadCredentialsException("Bad creds");

        when(authenticationManager.authenticate(authParams)).thenThrow(exception);

        assertThrows(org.springframework.security.core.AuthenticationException.class, () -> authService.login(request));

        verify(auditService).logFailure(eq("LOGIN_FAILURE"), eq("baduser"), eq("/api/auth/login"), anyString());
    }

    @Test
    void testRefresh_Success_ReturnsNewTokens() {
        when(userDetailsService.loadUserByUsername("testuser")).thenReturn(mockUserDetails);
        when(jwtService.generateToken(eq(mockUserDetails), anyLong()))
                .thenReturn("new-access-token")
                .thenReturn("new-refresh-token");

        AuthResponse response = authService.refresh("testuser");

        assertNotNull(response);
        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
    }

    @Test
    void testProcessForgotPassword_Success_GeneratesToken() {
        UserRecord userRecord = new UserRecord(1L, "testuser", "hash", Set.of(Role.ROLE_OPERATOR));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(userRecord));
        when(resetTokenRepository.countRecentRequests("testuser", 1)).thenReturn(0);

        authService.processForgotPassword("testuser");

        verify(resetTokenRepository).invalidatePreviousTokens(1L);
        verify(resetTokenRepository).save(any());
        verify(auditService).logSuccess(eq("FORGOT_PASSWORD_REQUEST"), eq("testuser"), anyString(), anyString());
    }
}
