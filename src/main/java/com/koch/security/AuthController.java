package com.koch.security;

import com.koch.security.model.AuthRequest;
import com.koch.security.model.AuthResponse;
import jakarta.validation.Valid;
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

    public AuthController(AuthService authService, JwtDecoder jwtDecoder) {
        this.authService = authService;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody com.koch.security.model.TokenRefreshRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.refreshToken());
            return ResponseEntity.ok(authService.refresh(jwt.getSubject()));
        } catch (JwtValidationException e) {
            return ResponseEntity.status(401).build();
        }
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@RequestBody com.koch.security.model.PasswordChangeRequest request, 
                                               java.security.Principal principal) {
        authService.changePassword(principal.getName(), request.oldPassword(), request.newPassword());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid com.koch.security.model.PasswordResetRequest request) {
        authService.processForgotPassword(request.username());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid com.koch.security.model.PasswordResetCompleteRequest request) {
        authService.resetPassword(request.token(), request.newPassword());
        return ResponseEntity.ok().build();
    }
}
