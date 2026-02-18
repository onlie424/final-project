package com.example.finalprojectb.Config;

import com.example.finalprojectb.model.User;
import com.example.finalprojectb.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitialiser implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitialiser.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        createDefaultAdmin();
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@example.com";

        // Always update admin password on startup (remove this check temporarily)
        User adminUser = userRepository.findByEmail(adminEmail).orElse(new User());

        if (adminUser.getId() == null) {
            // Creating new admin
            adminUser.setEmail(adminEmail);
            adminUser.setFullName("System Administrator");
            adminUser.setRole(User.Role.ADMIN);
            logger.info("Creating new admin user...");
        } else {
            // Updating existing admin password
            logger.info("Updating existing admin password...");
        }


        adminUser.setPassword(passwordEncoder.encode("yo123456"));
        userRepository.save(adminUser);

        logger.info("Admin user updated successfully!");
        logger.info("Email: {}", adminEmail);
        logger.info("Password: yo123456");
        logger.warn("Please change the default admin password after first login!");
    }
}