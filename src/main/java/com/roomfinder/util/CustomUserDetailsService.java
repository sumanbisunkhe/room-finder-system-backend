package com.roomfinder.util;

import com.roomfinder.entity.User;
import com.roomfinder.repository.UserRepository;
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

        // Convert User entity to Spring Security UserDetails
        return org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(Collections.singletonList(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                ))
                .accountExpired(!user.isActive())
                .accountLocked(!user.isActive())
                .credentialsExpired(false)
                .disabled(!user.isActive())
                .build();
    }
}