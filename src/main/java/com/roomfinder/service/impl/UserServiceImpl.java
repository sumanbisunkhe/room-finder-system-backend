package com.roomfinder.service.impl;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.dto.request.ValidateUsersRequest;
import com.roomfinder.dto.response.GrowthTrendResponse;
import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.exceptions.UserNotFoundException;
import com.roomfinder.repository.UserRepository;
import com.roomfinder.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        if (updatedRequest.getRole() != null) {
            user.setRole(updatedRequest.getRole());
        }

        if (updatedRequest.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(updatedRequest.getPassword()));
        }
        if (updatedRequest.getPassword() == null) {
            user.setPassword(user.getPassword());
        }

        // Update the updated timestamp
        user.setUpdatedAt(LocalDateTime.now());

        // Save the updated user in the repository
        return userRepository.save(user);
    }


    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Current password cannot be blank");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("New password cannot be blank");
        }
        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found"));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Current password is incorrect");
        }

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
    public Optional<User> loadUserById(Long userId) {
        try {
            Optional<User> user = userRepository.findById(userId);
            if (user.isPresent()) {
                return user;
            } else {
                throw new RuntimeException("User not found with id: " + userId);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error loading user with id: " + userId, e);
        }
    }


    @Override
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    @Override
    public Page<User> getAllSeekers(Pageable pageable) {
        return userRepository.findAllByRole(UserRole.SEEKER, pageable);
    }

    @Override
    public Page<User> getAllLandlords(Pageable pageable) {
        return userRepository.findAllByRole(UserRole.LANDLORD, pageable);
    }

    @Override
    public Page<User> getAllAdmins(Pageable pageable) {
        return userRepository.findAllByRole(UserRole.ADMIN, pageable);
    }

    @Override
    public Page<User> getAllSeekersAndLandlords(Pageable pageable) {
        return userRepository.findAllByRoleIn(List.of(UserRole.SEEKER, UserRole.LANDLORD), pageable);
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

    @Override
    public Page<User> searchUsers(String keyword, Pageable pageable) {
        return userRepository.searchByUsernameOrEmail(keyword, pageable);
    }

    @Override
    public Map<String, Long> getUserStatistics() {
        Map<String, Long> stats = new LinkedHashMap<>();

        stats.put("totalUsers", userRepository.count());
        stats.put("activeUsers", userRepository.countByIsActive(true));
        stats.put("inactiveUsers", userRepository.countByIsActive(false));
        stats.put("totalAdmins", userRepository.countByRole(UserRole.ADMIN));
        stats.put("totalLandlords", userRepository.countByRole(UserRole.LANDLORD));
        stats.put("totalSeekers", userRepository.countByRole(UserRole.SEEKER));

        return stats;
    }

    @Override
    public List<GrowthTrendResponse> getUserGrowthTrends(String interval) {
        List<Object[]> results;

        switch (interval.toLowerCase()) {
            case "daily" -> results = userRepository.findDailyGrowth();
            case "monthly" -> results = userRepository.findMonthlyGrowth();
            default -> throw new IllegalArgumentException("Invalid interval. Use 'daily' or 'monthly'.");
        }

        return results.stream()
                .map(row -> new GrowthTrendResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (String) row[2]
                ))
                .collect(Collectors.toList());
    }

    @Override
    public Page<User> getActiveUsers(Pageable pageable) {
        return userRepository.findAllByIsActive(true, pageable);

    }

    @Override
    public Page<User> getInactiveUsers(Pageable pageable) {
        return userRepository.findAllByIsActive(false, pageable);

    }

}
