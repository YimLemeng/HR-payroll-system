package com.hr.employee_management.Config;

import com.hr.employee_management.Entity.User;
import com.hr.employee_management.Repository.UserRepository;
import com.hr.employee_management.enums.Role;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BootstrapAdminConfig {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.username:}")
    private String bootstrapUsername;

    @Value("${app.bootstrap-admin.email:}")
    private String bootstrapEmail;

    @Value("${app.bootstrap-admin.password:}")
    private String bootstrapPassword;

    @Bean
    public CommandLineRunner bootstrapAdminRunner() {
        return args -> {
            if (bootstrapUsername == null || bootstrapUsername.isBlank() ||
                bootstrapPassword == null || bootstrapPassword.isBlank()) {
                log.info("No bootstrap admin credentials configured. Skipping initial admin provisioning.");
                return;
            }
            long adminCount = userRepository.countByRoleAndActiveTrue(Role.ADMIN);
            if (adminCount == 0) {
                if (!userRepository.existsByUsername(bootstrapUsername) && !userRepository.existsByEmail(bootstrapEmail)) {
                    User admin = User.builder()
                            .username(bootstrapUsername)
                            .email(bootstrapEmail)
                            .password(passwordEncoder.encode(bootstrapPassword))
                            .role(Role.ADMIN)
                            .active(true)
                            .build();
                    userRepository.save(admin);
                    log.info("Secure bootstrap initialized default initial ADMIN account: {}", bootstrapUsername);
                } else {
                    log.info("Initial admin username or email already exists. Skipping bootstrap admin creation.");
                }
            } else {
                log.info("System has {} active ADMIN account(s). Bootstrap skipped.", adminCount);
            }
        };
    }
}
