package com.koch.security;

import com.koch.security.model.AuthRequest;
import com.koch.security.model.AuthResponse;
import com.koch.security.model.TokenRefreshRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    public AuthResponse login(AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        
        // 15 minutes access, 7 days refresh
        String accessToken = jwtService.generateToken(userDetails, 15 * 60 * 1000);
        String refreshToken = jwtService.generateToken(userDetails, 7 * 24 * 60 * 60 * 1000);

        return new AuthResponse(accessToken, refreshToken, 900);
    }

    public AuthResponse refresh(TokenRefreshRequest request) {
        // Simple implementation: validate the refresh token and issue new pair
        // In a real production system, you'd check a blacklist or use Spring's OIDC support
        // For this Senior Architect demo, we'll parse and verify the refresh token.
        
        // We'll use the userDetailsService to load the user and re-issue based on the subject
        // Normally we'd use a separate verification service or Nimbus logic here.
        // For the sake of simplicity, we'll assume the refresh token's subject is the username.
        
        // Let's implement a verify token in JwtService next if needed.
        // But for now, we'll use a simplified version.
        
        // In this implementation, the auth controller will call this with the token.
        // We will assume the token is validated by the resource server logic or a separate check.
        
        return null; // Placeholder for now, I'll update it later
    }
}
