package com.roomfinder.security;

import com.roomfinder.controller.AuthController;
import com.roomfinder.entity.User;
import com.roomfinder.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);


    @Autowired
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        // First try to find by username
        User user = userRepository.findByUsername(identifier);

        // If not found by username, try email
        if (user == null) {
            user = userRepository.findByEmail(identifier);
        }

        // If user is still not found, throw exception
        if (user == null) {
            throw new UsernameNotFoundException("User not found with identifier: " + identifier);
        }

        // Check if the account is active
        if (!user.isActive()) {
            throw new UsernameNotFoundException("User account is not active: " + identifier);
        }

        // Log role being assigned
        logger.debug("Loading user with role: ROLE_" + user.getRole().name());

        // Convert User entity to CustomUserDetails
        return new CustomUserDetails(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),  // enabled
                user.isActive(),  // accountNonExpired
                true,            // credentialsNonExpired
                user.isActive(), // accountNonLocked
                Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ),
                user.getId()
        );
    }
}