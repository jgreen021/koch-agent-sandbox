package com.koch.security;

import com.koch.security.model.AuditLogRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);
    private final JdbcTemplate jdbcTemplate;

    public AuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void init() {
        // Ensure audit_logs table exists
        jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='audit_logs' AND xtype='U') " +
                "CREATE TABLE audit_logs (" +
                "id BIGINT IDENTITY(1,1) PRIMARY KEY, " +
                "timestamp DATETIME2 NOT NULL, " +
                "event NVARCHAR(100) NOT NULL, " +
                "username NVARCHAR(255), " +
                "path NVARCHAR(2000), " +
                "reason NVARCHAR(MAX))");
    }

    @Async
    public void logFailure(String event, String username, String path, String reason) {
        AuditLogRecord audit = new AuditLogRecord(LocalDateTime.now(), event, username, path, reason);
        saveAudit(audit);
    }

    private void saveAudit(AuditLogRecord audit) {
        try {
            jdbcTemplate.update(
                "INSERT INTO audit_logs (timestamp, event, username, path, reason) VALUES (?, ?, ?, ?, ?)",
                audit.timestamp(),
                audit.event(),
                audit.username(),
                audit.path(),
                audit.reason()
            );
            logger.info("Audit logged: {} - User: {} - Path: {}", audit.event(), audit.username(), audit.path());
        } catch (Exception e) {
            logger.error("Failed to persist audit log", e);
        }
    }
}
