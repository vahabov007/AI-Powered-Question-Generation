package com.vahabvahabov.AI_Powered_Question_Generation_Module.config;

import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.User;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.model.enums.UserRole;
import com.vahabvahabov.AI_Powered_Question_Generation_Module.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            if (userRepository.findByUsername("admin").isEmpty()) {
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRoles(Set.of(UserRole.ROLE_ADMIN, UserRole.ROLE_TEACHER));
                admin.setActive(true);
                userRepository.save(admin);

                System.out.println("Admin user created: admin/admin123");
            }

            if (userRepository.findByUsername("teacher").isEmpty()) {
                User teacher = new User();
                teacher.setUsername("teacher");
                teacher.setPassword(passwordEncoder.encode("teacher123"));
                teacher.setRoles(Set.of(UserRole.ROLE_TEACHER));
                teacher.setActive(true);
                userRepository.save(teacher);

                System.out.println("Teacher user created: teacher/teacher123");
            }
        };
    }
}