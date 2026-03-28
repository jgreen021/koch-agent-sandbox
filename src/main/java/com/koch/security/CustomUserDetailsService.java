package com.koch.security;

import com.koch.security.model.UserRecord;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CustomUserDetailsService.class);
    private final JdbcUserRepository userRepository;

    public CustomUserDetailsService(JdbcUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Loading user: {}", username);
        UserRecord userRecord = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User NOT found in DB: {}", username);
                    return new UsernameNotFoundException("User not found: " + username);
                });

        logger.info("User found: {}, password hash: {}", username, userRecord.password().substring(0, 7) + "...");
        return new User(
                userRecord.username(),
                userRecord.password(),
                userRecord.roles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.name()))
                        .collect(Collectors.toList())
        );
    }
}
