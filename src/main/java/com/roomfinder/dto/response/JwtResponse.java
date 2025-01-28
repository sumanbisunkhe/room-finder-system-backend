package com.roomfinder.dto.response;

import lombok.Data;

@Data
public class JwtResponse {
    private String token;


    // Constructors
    public JwtResponse(String token) {
        this.token = token;
    }

}