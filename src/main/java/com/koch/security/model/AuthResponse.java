package com.koch.security.model;

public record AuthResponse(
    String accessToken,
    String refreshToken,
    long expiresIn
) {}
