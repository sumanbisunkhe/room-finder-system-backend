package com.roomfinder.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.roomfinder.dto.request.PasswordChangeRequest;
import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.request.UpdateProfileRequest;
import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.exceptions.UserNotFoundException;
import com.roomfinder.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
class UserControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User testUser;
    private RegisterRequest registerRequest;
    private UpdateProfileRequest updateProfileRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();

        // Initialize test user
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("+1234567890")
                .role(UserRole.SEEKER)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isActive(true)
                .build();

        // Initialize register request
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setEmail("test@example.com");
        registerRequest.setFullName("Test User");
        registerRequest.setPhoneNumber("+1234567890");
        registerRequest.setRole("SEEKER");

        // Initialize update profile request
        updateProfileRequest = new UpdateProfileRequest();
        updateProfileRequest.setUsername("updateduser");
        updateProfileRequest.setEmail("updated@example.com");
        updateProfileRequest.setFullName("Updated User");
        updateProfileRequest.setPhoneNumber("+9876543210");
    }

    @Test
    void register_Success() throws Exception {
        when(userService.register(any(RegisterRequest.class))).thenReturn(testUser);

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully"));

        verify(userService).register(any(RegisterRequest.class));
    }

    @Test
    void register_UsernameTaken() throws Exception {
        when(userService.register(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("Username is already taken."));

        mockMvc.perform(post("/api/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Username is already taken."));
    }

    @Test
    void updateProfile_Success() throws Exception {
        when(userService.updateProfile(eq(1L), any(UpdateProfileRequest.class))).thenReturn(testUser);

        mockMvc.perform(put("/api/users/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profile updated successfully"));

        verify(userService).updateProfile(eq(1L), any(UpdateProfileRequest.class));
    }

    @Test
    void updateProfile_UserNotFound() throws Exception {
        when(userService.updateProfile(eq(1L), any(UpdateProfileRequest.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        mockMvc.perform(put("/api/users/1/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateProfileRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("User not found"));
    }

    @Test
    void changePassword_Success() throws Exception {
        PasswordChangeRequest passwordChangeRequest = new PasswordChangeRequest();
        passwordChangeRequest.setNewPassword("newPassword123");

        doNothing().when(userService).changePassword(eq(1L), anyString());

        mockMvc.perform(put("/api/users/1/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(passwordChangeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        verify(userService).changePassword(eq(1L), anyString());
    }

    @Test
    void deactivateAccount_Success() throws Exception {
        doNothing().when(userService).deactivateAccount(1L);

        mockMvc.perform(put("/api/users/1/deactivate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account deactivated successfully"));

        verify(userService).deactivateAccount(1L);
    }

    @Test
    void activateAccount_Success() throws Exception {
        doNothing().when(userService).activateAccount(1L);

        mockMvc.perform(put("/api/users/1/activate"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account activated successfully"));

        verify(userService).activateAccount(1L);
    }

    @Test
    void deleteAccount_Success() throws Exception {
        doNothing().when(userService).deleteAccount(1L);

        mockMvc.perform(delete("/api/users/1/delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Account deleted successfully"));

        verify(userService).deleteAccount(1L);
    }

    @Test
    void getAllUsers_Success() throws Exception {
        List<User> users = Arrays.asList(testUser);
        when(userService.getAllUsers()).thenReturn(users);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].email").value("test@example.com"));

        verify(userService).getAllUsers();
    }

    @Test
    void getAllSeekers_Success() throws Exception {
        List<User> seekers = Arrays.asList(testUser);
        when(userService.getAllSeekers()).thenReturn(seekers);

        mockMvc.perform(get("/api/users/seekers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("testuser"))
                .andExpect(jsonPath("$[0].role").value("SEEKER"));

        verify(userService).getAllSeekers();
    }

    @Test
    void getUserByUsername_Success() throws Exception {
        when(userService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/username/testuser"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User found"))
                .andExpect(jsonPath("$.data.username").value("testuser"));

        verify(userService).getUserByUsername("testuser");
    }

    @Test
    void getUserByEmail_Success() throws Exception {
        when(userService.getUserByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        mockMvc.perform(get("/api/users/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User found"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"));

        verify(userService).getUserByEmail("test@example.com");
    }
}