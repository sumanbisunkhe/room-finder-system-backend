package com.roomfinder.controller;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.PasswordChangeRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.dto.response.ApiResponse;
import com.roomfinder.entity.User;
import com.roomfinder.exceptions.UserNotFoundException;
import com.roomfinder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            User registeredUser = userService.register(registerRequest);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse(true, "User registered successfully"));
        } catch (IllegalArgumentException e) {
            // Handle specific exception, like username/email already taken
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            // Handle other exceptions
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred during registration"));
        }
    }

    // Update user profile
    @PutMapping("/{id}/update")
    public ResponseEntity<ApiResponse> updateProfile(@PathVariable Long id, @Valid @RequestBody UpdateProfileRequest updatedRequest) {
        try {
            User updatedUser = userService.updateProfile(id, updatedRequest); // Pass the UpdateProfileRequest instead of User
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage())); // For duplicate username or email
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while updating the profile"));
        }
    }

    // Change password
    @PutMapping("/{id}/change-password")
    public ResponseEntity<ApiResponse> changePassword(@PathVariable Long id, @Valid @RequestBody PasswordChangeRequest request) {
        try {
            userService.changePassword(id, request.getNewPassword());
            return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while changing the password"));
        }
    }

    // Deactivate account
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse> deactivateAccount(@PathVariable Long id) {
        try {
            userService.deactivateAccount(id);
            return ResponseEntity.ok(new ApiResponse(true, "Account deactivated successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while deactivating the account"));
        }
    }

    // Activate account
    @PutMapping("/{id}/activate")
    public ResponseEntity<ApiResponse> activateAccount(@PathVariable Long id) {
        try {
            userService.activateAccount(id);
            return ResponseEntity.ok(new ApiResponse(true, "Account activated successfully"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while activating the account"));
        }
    }
    // Delete account
    @DeleteMapping("/{id}/delete")
    public ResponseEntity<ApiResponse> deleteAccount(@PathVariable Long id) {
        try {
            userService.deleteAccount(id);
            return ResponseEntity.ok(new ApiResponse(true, "Account deleted successfully"));
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "An error occurred while deleting the account"));
        }
    }
    // Get all users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // Get users by role
    @GetMapping("/seekers")
    public ResponseEntity<List<User>> getAllSeekers() {
        return ResponseEntity.ok(userService.getAllSeekers());
    }

    @GetMapping("/landlords")
    public ResponseEntity<List<User>> getAllLandlords() {
        return ResponseEntity.ok(userService.getAllLandlords());
    }

    @GetMapping("/admins")
    public ResponseEntity<List<User>> getAllAdmins() {
        return ResponseEntity.ok(userService.getAllAdmins());
    }

    @GetMapping("/seekers-and-landlords")
    public ResponseEntity<List<User>> getAllSeekersAndLandlords() {
        return ResponseEntity.ok(userService.getAllSeekersAndLandlords());
    }

    // Get user by username (case-insensitive)
    @GetMapping("/username/{username}")
    public ResponseEntity<?> getUserByUsername(@PathVariable String username) {
        String normalizedUsername = username.trim().toLowerCase();
        Optional<User> user = userService.getUserByUsername(normalizedUsername);

        return user.map(value -> ResponseEntity.ok(new ApiResponse(true, "User found", value))).orElseGet(() -> ResponseEntity.badRequest().body(new ApiResponse(false, "User not found", null)));
    }

    // Get user by email
    @GetMapping("/email/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userService.getUserByEmail(email);
        return user.map(value -> ResponseEntity.ok(new ApiResponse(true, "User found", value))).orElseGet(() -> ResponseEntity.badRequest().body(new ApiResponse(false, "User not found", null)));
    }

}
