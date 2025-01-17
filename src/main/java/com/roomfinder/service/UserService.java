package com.roomfinder.service;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    public User register(RegisterRequest registerRequest);
    Optional<User> login(String username, String password);
    User updateProfile(Long userId, UpdateProfileRequest updatedRequest);
    void changePassword(Long userId,String newPassword);
    void deactivateAccount(Long userId);
    void activateAccount(Long userId);
    void deleteAccount(Long userId);

    List<User> getAllUsers();
    List<User> getAllSeekers();
    List<User> getAllLandlords();
    List<User> getAllAdmins();
    List<User> getAllSeekersAndLandlords();

    Optional<User> getUserByUsername(String username);
    Optional<User> getUserByEmail(String email);
}
