package com.koch.security;

import com.koch.security.model.Role;
import com.koch.security.model.UserRecord;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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
                    Long id = rs.getLong("ID");
                    String uname = rs.getString("USERNAME");
                    String pass = rs.getString("PASSWORD");
                    Set<Role> roles = getUserRoles(id);
                    return new UserRecord(id, uname, pass, roles);
                },
                username
        ).stream().findFirst();
    }

    public Optional<UserRecord> findById(Long id) {
        return jdbcTemplate.query(
                "SELECT u.id, u.username, u.password FROM app_users u WHERE u.id = ?",
                (rs, rowNum) -> {
                    Long userId = rs.getLong("ID");
                    String uname = rs.getString("USERNAME");
                    String pass = rs.getString("PASSWORD");
                    Set<Role> roles = getUserRoles(userId);
                    return new UserRecord(userId, uname, pass, roles);
                },
                id
        ).stream().findFirst();
    }

    private Set<Role> getUserRoles(Long userId) {
        return new HashSet<>(jdbcTemplate.query(
                "SELECT r.role_name FROM user_roles r WHERE r.user_id = ?",
                (rs, rowNum) -> Role.valueOf(rs.getString("ROLE_NAME")),
                userId
        ));
    }

    public void updatePassword(String username, String newHashedPassword) {
        int updated = jdbcTemplate.update(
                "UPDATE app_users SET password = ? WHERE username = ?",
                newHashedPassword,
                username
        );
        if (updated == 0) {
            throw new RuntimeException("User not found for password update: " + username);
        }
    }

    public void save(UserRecord user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO app_users (username, password) VALUES (?, ?)",
                new String[] {"ID"}
            );
            ps.setString(1, user.username());
            ps.setString(2, user.password());
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new RuntimeException("Failed to retrieve generated ID for user");
        }
        Long userId = key.longValue();

        for (Role role : user.roles()) {
            jdbcTemplate.update("INSERT INTO user_roles (user_id, role_name) VALUES (?, ?)", userId, role.name());
        }
    }

    // Provisioning helper
    public void ensureTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS app_users (" +
                "id NUMBER(19) GENERATED ALWAYS AS IDENTITY, " +
                "username NVARCHAR2(255) NOT NULL, " +
                "password NVARCHAR2(255) NOT NULL, " +
                "CONSTRAINT pk_app_users PRIMARY KEY (id), " +
                "CONSTRAINT unq_app_users_username UNIQUE (username))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS user_roles (" +
                "user_id NUMBER(19) NOT NULL, " +
                "role_name NVARCHAR2(100), " +
                "CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES app_users(id))");
    }

    public void ensureAuditTable() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS audit_logs (" +
                "id NUMBER(19) GENERATED ALWAYS AS IDENTITY, " +
                "timestamp TIMESTAMP(6) NOT NULL, " +
                "event NVARCHAR2(100) NOT NULL, " +
                "username NVARCHAR2(255), " +
                "path NVARCHAR2(2000), " +
                "reason NCLOB, " +
                "CONSTRAINT pk_audit_logs PRIMARY KEY (id))");
    }
}
