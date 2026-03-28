package com.koch.security.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request to initiate a password reset.
 * Identity is masked in the response to prevent enumeration.
 */
public record PasswordResetRequest(
    @NotBlank(message = "Username is required")
    String username
) {}
