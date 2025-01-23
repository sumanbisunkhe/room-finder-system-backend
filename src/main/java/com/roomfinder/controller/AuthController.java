package com.roomfinder.controller;

import com.roomfinder.dto.request.LoginRequest;
import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.response.JwtResponse;
import com.roomfinder.dto.response.MessageResponse;
import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.security.JwtUtil;
import com.roomfinder.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            // Log login attempt
            logger.info("Login attempt for identifier: {}", loginRequest.getIdentifier());

            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            // Log successful authentication details
            logger.info("Authentication successful for: {}", authentication.getName());
            logger.info("User authorities: {}", authentication.getAuthorities());

            SecurityContextHolder.getContext().setAuthentication(authentication);

            List<UserRole> userRoles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .map(UserRole::valueOf)
                    .collect(Collectors.toList());

            String jwt = jwtUtil.generateToken(
                    authentication.getName(),
                    userRoles
            );

            // Log token generation
            logger.info("JWT generated for user: {}", authentication.getName());

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            // Detailed error logging
            logger.error("Login error for identifier: {}", loginRequest.getIdentifier(), e);
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Invalid username or password"));
        }
    }
    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                String username = jwtUtil.extractUsername(jwt);

                if (username != null && jwtUtil.validateToken(jwt, username)) {
                    return ResponseEntity.ok(new MessageResponse("Token is valid"));
                }
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Invalid token"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error validating token"));
        }
    }
}