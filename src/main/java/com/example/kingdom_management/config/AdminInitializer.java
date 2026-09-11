package com.example.kingdom_management.config;

import com.example.kingdom_management.domain.User;
import com.example.kingdom_management.domain.enums.Role;
import com.example.kingdom_management.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminInitializer {

    private static final Logger log = LoggerFactory.getLogger(AdminInitializer.class);

    @Value("${app.security.initial-users.admin.username:}")
    private String adminUsername;

    @Value("${app.security.initial-users.admin.password:}")
    private String adminPassword;

    @Value("${app.security.initial-users.officer.username:}")
    private String officerUsername;

    @Value("${app.security.initial-users.officer.password:}")
    private String officerPassword;

    @Value("${app.security.initial-users.kingdom-member.username:}")
    private String kingdomMemberUsername;

    @Value("${app.security.initial-users.kingdom-member.password:}")
    private String kingdomMemberPassword;

    @Bean
    CommandLineRunner createInitialUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfConfigured(
                    userRepository,
                    passwordEncoder,
                    adminUsername,
                    adminPassword,
                    Role.ADMIN
            );

            createUserIfConfigured(
                    userRepository,
                    passwordEncoder,
                    officerUsername,
                    officerPassword,
                    Role.OFFICER
            );

            createUserIfConfigured(
                    userRepository,
                    passwordEncoder,
                    kingdomMemberUsername,
                    kingdomMemberPassword,
                    Role.KINGDOM_MEMBER
            );
        };
    }

    private void createUserIfConfigured(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            String password,
            Role role) {

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            log.debug("No initial {} account configured.", role);
            return;
        }

        if (userRepository.existsByUsername(username)) {
            log.debug("Initial {} account '{}' already exists.", role, username);
            return;
        }

        userRepository.save(new User(
                username,
                passwordEncoder.encode(password),
                role
        ));

        log.info("Initial {} account '{}' created.", role, username);
    }
}
