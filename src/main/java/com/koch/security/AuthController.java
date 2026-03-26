package com.koch.security;

import com.koch.security.model.AuthRequest;
import com.koch.security.model.AuthResponse;
import com.koch.security.model.TokenRefreshRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtDecoder jwtDecoder;
    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public AuthController(AuthService authService, JwtDecoder jwtDecoder, JwtService jwtService, CustomUserDetailsService userDetailsService) {
        this.authService = authService;
        this.jwtDecoder = jwtDecoder;
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody TokenRefreshRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.refreshToken());
            String username = jwt.getSubject();
            var userDetails = userDetailsService.loadUserByUsername(username);
            
            String accessToken = jwtService.generateToken(userDetails, 15 * 60 * 1000);
            String refreshToken = jwtService.generateToken(userDetails, 7 * 24 * 60 * 60 * 1000);

            return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, 900));
        } catch (JwtValidationException e) {
            return ResponseEntity.status(401).build();
        }
    }
}
