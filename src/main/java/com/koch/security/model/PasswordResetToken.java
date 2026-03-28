package com.koch.security.model;

import java.time.LocalDateTime;

/**
 * Audit-ready stateless token for password recovery.
 */
public record PasswordResetToken(
    Long id,
    Long userId,
    String token,
    LocalDateTime expiry,
    boolean used
) {}
