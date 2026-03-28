package com.koch.security;

import com.koch.security.model.AuthRequest;
import com.koch.security.model.AuthResponse;
import com.koch.security.model.PasswordResetToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.koch.security.exception.RateLimitExceededException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.security.SecureRandom;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;
    private final JdbcUserRepository userRepository;
    private final ResetTokenRepository resetTokenRepository;
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(AuthenticationManager authenticationManager, 
                       JwtService jwtService, 
                       CustomUserDetailsService userDetailsService,
                       JdbcUserRepository userRepository,
                       ResetTokenRepository resetTokenRepository,
                       org.springframework.security.crypto.password.PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    public AuthResponse login(AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            
            // 15 minutes access, 7 days refresh
            String accessToken = jwtService.generateToken(userDetails, 15 * 60 * 1000);
            String refreshToken = jwtService.generateToken(userDetails, 7 * 24 * 60 * 60 * 1000);

            return new AuthResponse(accessToken, refreshToken, 900);
        } catch (org.springframework.security.core.AuthenticationException e) {
            auditService.logFailure("LOGIN_FAILURE", request.username(), "/api/auth/login", e.getMessage());
            throw e;
        }
    }

    public void changePassword(String username, String oldPassword, String newPassword) {
        UserDetails user = userDetailsService.loadUserByUsername(username);
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            auditService.logFailure("PASSWORD_CHANGE_FAILURE", username, "/api/auth/password", "Old password mismatch");
            throw new RuntimeException("Invalid old password");
        }
        
        userRepository.updatePassword(username, passwordEncoder.encode(newPassword));
        auditService.logFailure("PASSWORD_CHANGE_SUCCESS", username, "/api/auth/password", "Success");
    }

    public AuthResponse refresh(String username) {
        // We trust the username provider by JwtController's decoder
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        String accessToken = jwtService.generateToken(userDetails, 15 * 60 * 1000);
        String refreshToken = jwtService.generateToken(userDetails, 7 * 24 * 60 * 60 * 1000);

        return new AuthResponse(accessToken, refreshToken, 900);
    }

    public void processForgotPassword(String username) {
        Optional<com.koch.security.model.UserRecord> userOpt = userRepository.findByUsername(username);
        
        // Rate Limiting: 3 requests per hour
        if (resetTokenRepository.countRecentRequests(username, 1) >= 3) {
            auditService.logFailure("FORGOT_PASSWORD_RATE_LIMIT", username, "/api/auth/forgot-password", "Rate limit exceeded");
            throw new RateLimitExceededException("Rate limit exceeded. Please try again later.");
        }

        if (userOpt.isPresent()) {
            com.koch.security.model.UserRecord user = userOpt.get();
            resetTokenRepository.invalidatePreviousTokens(user.id());
            
            String token = generateSecureToken();
            
            PasswordResetToken resetToken = new PasswordResetToken(
                null, 
                user.id(), 
                token, 
                LocalDateTime.now().plusMinutes(30), 
                false
            );
            
            resetTokenRepository.save(resetToken);
            
            // Simulation: Log to console for development
            logger.info("Password reset token for {}: {}", username, token);
            auditService.logFailure("FORGOT_PASSWORD_REQUEST", username, "/api/auth/forgot-password", "Token generated");
        } else {
            // Masking: Log but don't reveal user existence
            auditService.logFailure("FORGOT_PASSWORD_REQUEST_GHOST", username, "/api/auth/forgot-password", "User not found");
        }
    }

    public void resetPassword(String tokenValue, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new RuntimeException("Password must be at least 8 characters");
        }

        PasswordResetToken token = resetTokenRepository.findByToken(tokenValue)
            .orElseThrow(() -> new RuntimeException("Invalid or expired token"));

        if (token.expiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token has expired");
        }

        com.koch.security.model.UserRecord user = userRepository.findById(token.userId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.updatePassword(user.username(), passwordEncoder.encode(newPassword));
        resetTokenRepository.markAsUsed(tokenValue);
        
        auditService.logFailure("PASSWORD_RESET_SUCCESS", user.username(), "/api/auth/reset-password", "Success");
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
