package com.roomfinder.controller;

import com.roomfinder.dto.request.LoginRequest;
import com.roomfinder.dto.request.RegisterRequest;
import com.roomfinder.dto.response.JwtResponse;
import com.roomfinder.dto.response.MessageResponse;
import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import com.roomfinder.security.JwtUtil;
import com.roomfinder.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
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
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginRequest loginRequest,
            HttpServletResponse response) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getIdentifier(),
                            loginRequest.getPassword()
                    )
            );

            List<UserRole> userRoles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(role -> role.replace("ROLE_", ""))
                    .map(UserRole::valueOf)
                    .collect(Collectors.toList());

            String jwt = jwtUtil.generateToken(authentication.getName(), userRoles);

            // Use proper cookie construction
            ResponseCookie cookie = ResponseCookie.from("jwt", jwt)
                    .httpOnly(true)
                    .secure(false) // Set to true in production
                    .path("/")
                    .maxAge(24 * 60 * 60)
                    .sameSite("Lax") // Important for cross-site requests
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

            return ResponseEntity.ok(new JwtResponse(jwt));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Error: Invalid credentials"));
        }
    }

    // Add logout endpoint
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(true);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0); // Expire immediately

        response.addCookie(jwtCookie);
        return ResponseEntity.ok(new MessageResponse("Logged out successfully"));
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