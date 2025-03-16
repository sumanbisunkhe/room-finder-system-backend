package com.roomfinder.security;

import com.roomfinder.enums.UserRole;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {
    private static final Logger logger = LoggerFactory.getLogger(JwtRequestFilter.class);

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private JwtUtil jwtUtil;

    // Define endpoints that don't require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/users/register",
            "/api/auth/login",
            "/ws", "/ws/**"  // WebSocket endpoints for handshake
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        logger.debug("Processing request to: {}", requestURI);

        // Bypass filter for public endpoints
        if (isPublicEndpoint(requestURI)) {
            chain.doFilter(request, response);
            return;
        }

        // Extract JWT from appropriate sources
        String jwtToken = extractJwtFromRequest(request);

        if (jwtToken == null) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Missing authentication token");
            return;
        }

        try {
            authenticateRequest(request, jwtToken);
        } catch (ExpiredJwtException ex) {
            handleTokenExpiration(response, ex);
            return;
        } catch (Exception ex) {
            handleAuthenticationError(response, ex);
            return;
        }

        chain.doFilter(request, response);
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        // 1. Check Authorization header first (for WebSocket/STOMP)
        String headerToken = extractTokenFromHeader(request);
        if (headerToken != null) return headerToken;

        // 2. Check cookies (for traditional HTTP requests)
        String cookieToken = extractTokenFromCookies(request);
        if (cookieToken != null) return cookieToken;

        // 3. Check URL parameter (alternative for non-header clients)
        return extractTokenFromParams(request);
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private String extractTokenFromParams(HttpServletRequest request) {
        return request.getParameter("token");
    }

    private void authenticateRequest(HttpServletRequest request, String jwtToken) {
        String username = jwtUtil.extractUsername(jwtToken);
        List<UserRole> roles = jwtUtil.extractRoles(jwtToken);

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // For WebSocket, create lightweight authentication
            if (isWebSocketRequest(request)) {
                createWebSocketAuthentication(username, roles, request);
            } else {
                // Traditional HTTP authentication
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                validateHttpAuthentication(jwtToken, userDetails, request);
            }
        }
    }

    private boolean isWebSocketRequest(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/ws");
    }

    private void createWebSocketAuthentication(String username, List<UserRole> roles, HttpServletRequest request) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .collect(Collectors.toList());

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(username, null, authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.info("WebSocket user authenticated: {}", username);
    }

    private void validateHttpAuthentication(String jwtToken, UserDetails userDetails, HttpServletRequest request) {
        if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("HTTP user authenticated: {}", userDetails.getUsername());
        }
    }

    private boolean isPublicEndpoint(String requestURI) {
        return PUBLIC_ENDPOINTS.stream().anyMatch(requestURI::startsWith);
    }

    private void handleTokenExpiration(HttpServletResponse response, ExpiredJwtException ex) throws IOException {
        logger.warn("JWT Token expired: {}", ex.getMessage());
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token expired");
    }

    private void handleAuthenticationError(HttpServletResponse response, Exception ex) throws IOException {
        logger.error("Authentication error: {}", ex.getMessage());
        sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
    }

    private void sendErrorResponse(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write(String.format(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}",
                status.value(),
                status.getReasonPhrase(),
                message
        ));
    }
}