package com.roomfinder.dto.request;

import com.roomfinder.enums.UserRole;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters.")
    private String username;

    @Email(message = "Invalid email format.")
    private String email;

    @Size(max = 100, message = "Full name must not exceed 100 characters.")
    private String fullName;

    @Pattern(
            regexp = "^\\+?[0-9]{10,15}$",
            message = "Phone number must be valid and can include an optional '+' sign."
    )
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
