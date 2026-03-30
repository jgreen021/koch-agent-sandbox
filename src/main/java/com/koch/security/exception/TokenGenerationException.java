package com.koch.security.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Thrown when JWT token generation or signing fails due to cryptographic issues
 * or missing RSA keys.
 */
public class TokenGenerationException extends AuthenticationException {
    public TokenGenerationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public TokenGenerationException(String msg) {
        super(msg);
    }
}
