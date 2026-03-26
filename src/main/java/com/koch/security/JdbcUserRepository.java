package com.koch.security;

import com.koch.security.model.Role;
import com.koch.security.model.UserRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class JdbcUserRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserRecord> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT u.id, u.username, u.password FROM app_users u WHERE u.username = ?",
                (rs, rowNum) -> {
                    Long id = rs.getLong("id");
                    String uname = rs.getString("username");
                    String pass = rs.getString("password");
                    Set<Role> roles = getUserRoles(id);
                    return new UserRecord(id, uname, pass, roles);
                },
                username
        ).stream().findFirst();
    }

    private Set<Role> getUserRoles(Long userId) {
        return new HashSet<>(jdbcTemplate.query(
                "SELECT r.role_name FROM user_roles r WHERE r.user_id = ?",
                (rs, rowNum) -> Role.valueOf(rs.getString("role_name")),
                userId
        ));
    }

    public void save(UserRecord user) {
        // Simple insert for the sake of demo/init
        jdbcTemplate.update(
            "INSERT INTO app_users (username, password) VALUES (?, ?)",
            user.username(),
            user.password()
        );
        Long userId = jdbcTemplate.queryForObject("SELECT @@IDENTITY", Long.class);
        for (Role role : user.roles()) {
            jdbcTemplate.update("INSERT INTO user_roles (user_id, role_name) VALUES (?, ?)", userId, role.name());
        }
    }

    // Provisioning helper
    public void ensureTables() {
        jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='app_users' AND xtype='U') " +
                "CREATE TABLE app_users (id BIGINT IDENTITY(1,1) PRIMARY KEY, username NVARCHAR(255) UNIQUE NOT NULL, password NVARCHAR(255) NOT NULL)");
        jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='user_roles' AND xtype='U') " +
                "CREATE TABLE user_roles (user_id BIGINT FOREIGN KEY REFERENCES app_users(id), role_name NVARCHAR(100))");
    }

    public void ensureAuditTable() {
        jdbcTemplate.execute("IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='audit_logs' AND xtype='U') " +
                "CREATE TABLE audit_logs (id BIGINT IDENTITY(1,1) PRIMARY KEY, timestamp DATETIME2, event NVARCHAR(100), username NVARCHAR(255), path NVARCHAR(255), reason NVARCHAR(MAX))");
    }
}
