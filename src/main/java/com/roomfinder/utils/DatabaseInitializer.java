package com.roomfinder.utils;

import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer implements CommandLineRunner {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public DatabaseInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create ADMIN user
        createUserIfNotExists(
                "Suman",
                "thesumanbisunkhe@gmail.com",
                "thesumanbisunkhe@gmail.com",
                "Suman Bisunkhe",
                "9800000002",
                UserRole.ADMIN
        );

        // Create LANDLORD user
        createUserIfNotExists(
                "landlord",
                "landlord@gmail.com",
                "landlord@gmail.com",
                "Land Lord",
                "9800000001",
                UserRole.LANDLORD
        );

        // Create SEEKER user
        createUserIfNotExists(
                "seeker@gmail.com",
                "seeker@gmail.com",
                "seeker@gmail.com",
                "Test User",
                "9800000000",
                UserRole.SEEKER
        );
    }

    private void createUserIfNotExists(String username, String email, String rawPassword,
                                       String fullName, String phoneNumber, UserRole role) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .email(email)
                    .password(passwordEncoder.encode(rawPassword))
                    .fullName(fullName)
                    .phoneNumber(phoneNumber)
                    .role(role)
                    .isActive(true)
                    .build();
            userRepository.save(user);
            System.out.println("User created: " + username);
        } else {
            System.out.println("User already exists: " + username);
        }
    }
}
