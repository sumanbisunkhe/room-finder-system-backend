package com.roomfinder.service.impl;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.dto.request.ValidateUsersRequest;
import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.exceptions.UserNotFoundException;
import com.roomfinder.repository.UserRepository;
import com.roomfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest registerRequest) {
        // Check if the username or email is already in use
        if (userRepository.findByUsername(registerRequest.getUsername()) != null) {
            throw new IllegalArgumentException("Username is already taken.");
        }
        if (userRepository.findByEmail(registerRequest.getEmail()) != null) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        // Map RegisterRequest to User entity
        User user = User.builder()
                .username(registerRequest.getUsername())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .email(registerRequest.getEmail())
                .fullName(registerRequest.getFullName())
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(UserRole.valueOf(registerRequest.getRole().toUpperCase()))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // Save the user in the repository
        return userRepository.save(user);
    }

    @Override
    public Optional<User> login(String username, String password) {
        Optional<User> user = Optional.ofNullable(userRepository.findByUsername(username));
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }
        return Optional.empty();
    }

    @Override
    public User updateProfile(Long userId, UpdateProfileRequest updatedRequest) {
        // Fetch the existing user from the database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        // Check if the username is unique, excluding the current user
        if (updatedRequest.getUsername() != null && !updatedRequest.getUsername().equals(user.getUsername())) {
            if (userRepository.findByUsername(updatedRequest.getUsername()) != null &&
                    !userRepository.findByUsername(updatedRequest.getUsername()).getId().equals(userId)) {
                throw new IllegalArgumentException("Username is already taken.");
            }
            user.setUsername(updatedRequest.getUsername());
        }

        // Check if the email is unique, excluding the current user
        if (updatedRequest.getEmail() != null && !updatedRequest.getEmail().equals(user.getEmail())) {
            if (userRepository.findByEmail(updatedRequest.getEmail()) != null &&
                    !userRepository.findByEmail(updatedRequest.getEmail()).getId().equals(userId)) {
                throw new IllegalArgumentException("Email is already registered.");
            }
            user.setEmail(updatedRequest.getEmail());
        }

        // Only update fields that are provided (non-null)
        if (updatedRequest.getFullName() != null) {
            user.setFullName(updatedRequest.getFullName());
        }
        if (updatedRequest.getPhoneNumber() != null) {
            user.setPhoneNumber(updatedRequest.getPhoneNumber());
        }

        // Update the updated timestamp
        user.setUpdatedAt(LocalDateTime.now());

        // Save the updated user in the repository
        return userRepository.save(user);
    }


    @Override
    public void changePassword(Long userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be null or blank");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        if (!user.isActive()) {
            throw new IllegalStateException("Cannot change password for an inactive user");
        }

        // Encode the new password before saving
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        try {
            userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update password", e);
        }
    }

    @Override
    public void deactivateAccount(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(false);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }
    @Override
    public void activateAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        if (user.isActive()) {
            throw new IllegalStateException("Account is already active");
        }

        user.setActive(true);
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Override
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        userRepository.delete(user);
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> getAllSeekers() {
        return userRepository.findAllByRole(UserRole.SEEKER);
    }

    @Override
    public List<User> getAllLandlords() {
        return userRepository.findAllByRole(UserRole.LANDLORD);
    }

    @Override
    public List<User> getAllAdmins() {
        return userRepository.findAllByRole(UserRole.ADMIN);
    }

    @Override
    public List<User> getAllSeekersAndLandlords() {
        return userRepository.findAllByRoleIn(List.of(UserRole.SEEKER, UserRole.LANDLORD));
    }

    @Override
    public Optional<User> getUserByUsername(String username) {
        return Optional.ofNullable(userRepository.findByUsernameIgnoreCase(username));
    }

    @Override
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(userRepository.findByEmail(email));
    }

    @Override
    public ValidateUsersRequest getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        return ValidateUsersRequest.builder()
                .userId(user.getId())
                .role(user.getRole().name())
                .build();
    }

}
