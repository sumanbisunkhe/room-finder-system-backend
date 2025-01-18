package com.roomfinder.config;

import com.roomfinder.security.JwtRequestFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtRequestFilter jwtRequestFilter;

    @Autowired
    public SecurityConfig(JwtRequestFilter jwtRequestFilter) {
        this.jwtRequestFilter = jwtRequestFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/api/users/register",
                                "/api/auth/login"
                        ).permitAll()

                        .requestMatchers(
                                "/api/bookings/{id}/approve",
                                "/api/bookings/{id}/reject"
                        ).hasRole("LANDLORD")

                        .requestMatchers(
                                "/api/bookings",
                                "/api/bookings/{id}/cancel"
                        ).hasRole("SEEKER")

                        .requestMatchers(
                                "/api/bookings/{id}",
                                "/api/bookings/room/{roomId}",
                                "/api/bookings/room/{roomId}/pending"
                        ).hasAnyRole("LANDLORD", "SEEKER")

                        .requestMatchers(
                                "/api/users",
                                "/api/users/admins",
                                "/api/users/seekers",
                                "/api/users/landlords",
                                "/api/users/seekers-and-landlords",
                                "/api/users/{id}/activate",
                                "/api/users/{id}/deactivate",
                                "/api/users/{id}/delete"
                        ).hasRole("ADMIN")
                        .requestMatchers(
                                "/api/users/{id}/update",
                                "/api/users/{id}/change-password"
                        ).hasAnyRole("ADMIN", "SEEKER", "LANDLORD")
                        .requestMatchers(
                                "/api/users/username/{username}",
                                "/api/users/email/{email}"
                        ).hasAnyRole("ADMIN", "LANDLORD")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(handler -> handler
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendError(
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    "Authentication is required to access this resource."
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.sendError(
                                    HttpServletResponse.SC_FORBIDDEN,
                                    "You do not have permission to access this resource."
                            );
                        })
                )
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                );

        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
