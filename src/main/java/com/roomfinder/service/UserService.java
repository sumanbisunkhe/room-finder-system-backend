package com.roomfinder.service;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.dto.request.ValidateUsersRequest;
import com.roomfinder.dto.response.GrowthTrendResponse;
import com.roomfinder.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface UserService {
    public User register(RegisterRequest registerRequest);

    Optional<User> login(String username, String password);

    User updateProfile(Long userId, UpdateProfileRequest updatedRequest);

    void changePassword(Long userId, String currentPassword, String newPassword);

    void deactivateAccount(Long userId);

    void activateAccount(Long userId);

    void deleteAccount(Long userId);

    Optional<User> loadUserById(Long userId);


    Page<User> getAllUsers(Pageable pageable);

    Page<User> getAllSeekers(Pageable pageable);

    Page<User> getAllLandlords(Pageable pageable);

    Page<User> getAllAdmins(Pageable pageable);

    Page<User> getAllSeekersAndLandlords(Pageable pageable);

    Optional<User> getUserByUsername(String username);

    Optional<User> getUserByEmail(String email);

    ValidateUsersRequest getUserById(Long senderId);

    Page<User> searchUsers(String keyword, Pageable pageable);

    Map<String, Long> getUserStatistics();

    List<GrowthTrendResponse> getUserGrowthTrends(String interval);

    Page<User> getActiveUsers(Pageable pageable);

    Page<User> getInactiveUsers(Pageable pageable);


}
