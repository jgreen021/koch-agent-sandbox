package com.koch.security;

import com.koch.security.model.PasswordResetToken;
import com.koch.security.model.UserRecord;
import com.koch.security.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.koch.security.exception.RateLimitExceededException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthServiceResetTest {

    private AuthService authService;
    private JdbcUserRepository userRepository;
    private ResetTokenRepository resetTokenRepository;
    private PasswordEncoder passwordEncoder;
    private AuditService auditService;

    @BeforeEach
    void setUp() {
        userRepository = mock(JdbcUserRepository.class);
        resetTokenRepository = mock(ResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        auditService = mock(AuditService.class);
        
        authService = new AuthService(
                mock(AuthenticationManager.class),
                mock(JwtService.class),
                mock(CustomUserDetailsService.class),
                userRepository,
                resetTokenRepository,
                passwordEncoder,
                auditService
        );
    }

    @Test
    void processForgotPassword_ShouldGenerateToken_WhenUserExists() {
        UserRecord user = new UserRecord(1L, "admin", "hash", Set.of(Role.ROLE_GATEWAY_ADMIN));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(resetTokenRepository.countRecentRequests("admin", 1)).thenReturn(0);

        authService.processForgotPassword("admin");

        verify(resetTokenRepository).save(any(PasswordResetToken.class));
        verify(resetTokenRepository).invalidatePreviousTokens(1L);
    }

    @Test
    void processForgotPassword_ShouldNotThrow_WhenUserMissing() {
        // Masking test
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> authService.processForgotPassword("ghost"));
        verify(resetTokenRepository, never()).save(any());
    }

    @Test
    void processForgotPassword_ShouldThrow_WhenRateLimitExceeded() {
        when(resetTokenRepository.countRecentRequests("admin", 1)).thenReturn(3);

        Exception exception = assertThrows(RateLimitExceededException.class, () -> authService.processForgotPassword("admin"));
        assertTrue(exception.getMessage().contains("Rate limit exceeded"));
    }

    @Test
    void resetPassword_ShouldUpdateHash_WhenTokenValid() {
        PasswordResetToken token = new PasswordResetToken(1L, 1L, "token123", LocalDateTime.now().plusMinutes(10), false);
        UserRecord user = new UserRecord(1L, "admin", "oldHash", Set.of(Role.ROLE_GATEWAY_ADMIN));
        
        when(resetTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Secret123!")).thenReturn("newHash");

        authService.resetPassword("token123", "Secret123!");

        verify(userRepository).updatePassword("admin", "newHash");
        verify(resetTokenRepository).markAsUsed("token123");
    }

    @Test
    void resetPassword_ShouldThrow_WhenTokenExpired() {
        PasswordResetToken token = new PasswordResetToken(1L, 1L, "token123", LocalDateTime.now().minusMinutes(1), false);
        when(resetTokenRepository.findByToken("token123")).thenReturn(Optional.of(token));

        Exception exception = assertThrows(RuntimeException.class, () -> authService.resetPassword("token123", "NewPass123!"));
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void resetPassword_ShouldThrow_WhenPasswordTooWeak() {
        PasswordResetToken token = new PasswordResetToken(1L, 1L, "token123", LocalDateTime.now().plusMinutes(10), false);
        // Repository should not even be called if validation fails, or AuthService checks it
        
        Exception exception = assertThrows(RuntimeException.class, () -> authService.resetPassword("token123", "short"));
        assertTrue(exception.getMessage().contains("at least 8 characters"));
    }
}
