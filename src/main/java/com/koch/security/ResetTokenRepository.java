package com.koch.security;

import com.koch.security.model.PasswordResetToken;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * JDBC-backed repository for managing password reset tokens in Oracle 23c.
 */
@Repository
public class ResetTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public ResetTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(PasswordResetToken token) {
        jdbcTemplate.update(
            "INSERT INTO PASSWORD_RESET_TOKENS (USER_ID, TOKEN, EXPIRY, USED) VALUES (?, ?, ?, ?)",
            token.userId(),
            token.token(),
            token.expiry(),
            token.used()
        );
    }

    public Optional<PasswordResetToken> findByToken(String token) {
        return jdbcTemplate.query(
            "SELECT ID, USER_ID, TOKEN, EXPIRY, USED FROM PASSWORD_RESET_TOKENS WHERE TOKEN = ? AND USED = FALSE",
            (rs, rowNum) -> new PasswordResetToken(
                rs.getLong("ID"),
                rs.getLong("USER_ID"),
                rs.getString("TOKEN"),
                rs.getTimestamp("EXPIRY").toLocalDateTime(),
                rs.getBoolean("USED")
            ),
            token
        ).stream().findFirst();
    }

    public void markAsUsed(String token) {
        jdbcTemplate.update("UPDATE PASSWORD_RESET_TOKENS SET USED = TRUE WHERE TOKEN = ?", token);
    }

    public void invalidatePreviousTokens(Long userId) {
        jdbcTemplate.update("UPDATE PASSWORD_RESET_TOKENS SET USED = TRUE WHERE USER_ID = ? AND USED = FALSE", userId);
    }

    public int countRecentRequests(String username, int hours) {
        return jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM PASSWORD_RESET_TOKENS prt " +
            "JOIN APP_USERS u ON prt.USER_ID = u.ID " +
            "WHERE u.USERNAME = ? AND prt.EXPIRY > ?",
            Integer.class,
            username,
            LocalDateTime.now().minusHours(hours)
        );
    }
}
