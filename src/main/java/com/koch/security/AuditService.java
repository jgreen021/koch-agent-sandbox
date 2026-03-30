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
        // Ensure audit_logs table exists using Oracle 23c syntax
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                "id NUMBER(19) GENERATED ALWAYS AS IDENTITY, " +
                "timestamp TIMESTAMP(6) NOT NULL, " +
                "event NVARCHAR2(100) NOT NULL, " +
                "username NVARCHAR2(255), " +
                "path NVARCHAR2(2000), " +
                "reason NCLOB, " +
                "CONSTRAINT pk_audit_logs PRIMARY KEY (id))");
    }

    @Async
    public void logFailure(String event, String username, String path, String reason) {
        logEvent("FAILURE_" + event, username, path, reason);
    }

    @Async
    public void logSuccess(String event, String username, String path, String details) {
        logEvent("SUCCESS_" + event, username, path, details);
    }

    @Async
    public void logEvent(String event, String username, String path, String details) {
        AuditLogRecord audit = new AuditLogRecord(LocalDateTime.now(), event, username, path, details);
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
