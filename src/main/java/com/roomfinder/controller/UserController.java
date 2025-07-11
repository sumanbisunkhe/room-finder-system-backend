package com.roomfinder.controller;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.PasswordChangeRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.dto.response.ApiResponse;
import com.roomfinder.dto.response.GrowthTrendResponse;
import com.roomfinder.entity.User;
import com.roomfinder.exceptions.UserNotFoundException;
import com.roomfinder.security.JwtUtil;
import com.roomfinder.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/current")
    public ResponseEntity<ApiResponse> getCurrentUser(@CookieValue(name = "jwt", required = false) String token) {
        try {
            if (token == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ApiResponse(false, "No token found"));
            }

            String username = jwtUtil.extractUsername(token);
            Optional<User> userOptional = userService.getUserByUsername(username);

            return userOptional.map(user -> ResponseEntity.ok(new ApiResponse(true, "User retrieved successfully", user))).orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "User not found")));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error fetching current user: " + e.getMessage()));
        }
    }

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
            userService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
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

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable Long id) {
        Optional<User> userOptional = userService.loadUserById(id);
        return userOptional.map(user -> ResponseEntity.ok(new ApiResponse(true, "User found", user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "User not found with ID: " + id)));
    }

    // Get all users with pagination
    @GetMapping
    public ResponseEntity<Page<User>> getAllUsers(
            @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(userService.getAllUsers(pageable));
    }

    // Get paginated seekers
    @GetMapping("/seekers")
    public ResponseEntity<Page<User>> getAllSeekers(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllSeekers(pageable));
    }

    // Get paginated landlords
    @GetMapping("/landlords")
    public ResponseEntity<Page<User>> getAllLandlords(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllLandlords(pageable));
    }

    // Get paginated admins
    @GetMapping("/admins")
    public ResponseEntity<Page<User>> getAllAdmins(@PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(userService.getAllAdmins(pageable));
    }

    // Get paginated seekers & landlords
    @GetMapping("/seekers-and-landlords")
    public ResponseEntity<Page<User>> getAllSeekersAndLandlords(@PageableDefault Pageable pageable) {
        return ResponseEntity.ok(userService.getAllSeekersAndLandlords(pageable));
    }

    //get paginates active users
    @GetMapping("/active")
    public ResponseEntity<Page<User>> getAllActiveUsers(@PageableDefault Pageable pageable) {

        return ResponseEntity.ok(userService.getActiveUsers(pageable));
    }

    //get paginated inactive users
    @GetMapping("/inactive")
    public ResponseEntity<Page<User>> getAllInactiveUsers(@PageableDefault Pageable pageable) {

        return ResponseEntity.ok(userService.getInactiveUsers(pageable));
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

    // Search user by username or email
    @GetMapping("/search")
    public ResponseEntity<ApiResponse> searchUsers(
            @RequestParam String keyword,
            @PageableDefault Pageable pageable) {
        Page<User> users = userService.searchUsers(keyword, pageable);
        return ResponseEntity.ok(new ApiResponse(true, "Users found", users));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse> getUserStatistics() {
        try {
            Map<String, Long> stats = userService.getUserStatistics();
            return ResponseEntity.ok(
                    new ApiResponse(true, "Statistics retrieved successfully", stats)
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error retrieving statistics: " + e.getMessage()));
        }
    }

    @GetMapping("/growth-trends")
    public ResponseEntity<ApiResponse> getGrowthTrends(
            @RequestParam(defaultValue = "monthly") String interval
    ) {
        try {
            List<GrowthTrendResponse> trends = userService.getUserGrowthTrends(interval);
            return ResponseEntity.ok(
                    new ApiResponse(true, "User growth trends retrieved", trends)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new ApiResponse(false, "Error retrieving trends"));
        }
    }
}
