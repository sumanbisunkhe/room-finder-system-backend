package com.roomfinder.service;

import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.exceptions.UserNotFoundException;
import com.roomfinder.repository.UserRepository;
import com.roomfinder.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private RegisterRequest registerRequest;
    private UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("encoded_password")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .role(UserRole.SEEKER)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("newuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("new@example.com");
        registerRequest.setFullName("New User");
        registerRequest.setPhoneNumber("+1987654321");
        registerRequest.setRole("SEEKER");

        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setUsername("updateduser");
        updateProfileRequest.setEmail("updated@example.com");
        updateProfileRequest.setFullName("Updated User");
        updateProfileRequest.setPhoneNumber("+1122334455");
    }

    @Test
    void register_Success() {
        when(userRepository.findByUsername(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(null);
        when(passwordEncoder.encode(any())).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals(testUser.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameExists_ThrowsException() {
        when(userRepository.findByUsername(any())).thenReturn(testUser);

        assertThrows(IllegalArgumentException.class, () -> userService.register(registerRequest));
    }

    @Test
    void login_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(true);

        Optional<User> result = userService.login("testuser", "password123");

        assertTrue(result.isPresent());
        assertEquals(testUser, result.get());
    }

    @Test
    void login_InvalidCredentials_ReturnsEmpty() {
        when(userRepository.findByUsername("testuser")).thenReturn(testUser);
        when(passwordEncoder.matches(any(), any())).thenReturn(false);

        Optional<User> result = userService.login("testuser", "wrongpassword");

        assertTrue(result.isEmpty());
    }

    @Test
    void updateProfile_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername(any())).thenReturn(null);
        when(userRepository.findByEmail(any())).thenReturn(null);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.updateProfile(1L, updateProfileRequest);

        assertNotNull(result);
        assertEquals(updateProfileRequest.getUsername(), result.getUsername());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateProfile_UserNotFound_ThrowsException() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateProfile(1L, updateProfileRequest));
    }

    @Test
    void changePassword_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(any())).thenReturn("new_encoded_password");

        assertDoesNotThrow(() -> userService.changePassword(1L, "newPassword123"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void getAllUsers_Success() {
        List<User> userList = Arrays.asList(testUser);
        when(userRepository.findAll()).thenReturn(userList);

        List<User> result = userService.getAllUsers();

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    void deactivateAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.deactivateAccount(1L));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void activateAccount_Success() {
        testUser.setActive(false);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        assertDoesNotThrow(() -> userService.activateAccount(1L));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deleteAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).delete(any(User.class));

        assertDoesNotThrow(() -> userService.deleteAccount(1L));
        verify(userRepository).delete(any(User.class));
    }
}