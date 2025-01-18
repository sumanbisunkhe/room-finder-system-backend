package com.roomfinder.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateUsersRequest {
    private Long userId;
    private String role;

    @JsonIgnore
    public boolean isValidRole() {
        return role != null && (role.equals("SEEKER") || role.equals("LANDLORD"));
    }
}