package com.koch.security.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("audit_logs")
public record AuditLogRecord(
    @Id Long id,
    LocalDateTime timestamp,
    String event,
    String username,
    String path,
    String reason
) {
    public AuditLogRecord(LocalDateTime timestamp, String event, String username, String path, String reason) {
        this(null, timestamp, event, username, path, reason);
    }
}
