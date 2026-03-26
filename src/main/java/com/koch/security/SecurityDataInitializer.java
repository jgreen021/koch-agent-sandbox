package com.koch.security;

import com.koch.security.model.Role;
import com.koch.security.model.UserRecord;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

@Component
public class SecurityDataInitializer implements CommandLineRunner {

    private final JdbcUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SecurityDataInitializer(JdbcUserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        userRepository.ensureTables();
        userRepository.ensureAuditTable(); // Make sure audit table is also ready

        if (userRepository.findByUsername("admin").isEmpty()) {
            UserRecord admin = new UserRecord(
                null, 
                "admin", 
                passwordEncoder.encode("password123"), 
                Set.of(Role.ROLE_GATEWAY_ADMIN, Role.ROLE_OPERATOR)
            );
            userRepository.save(admin);
        }
    }
}
