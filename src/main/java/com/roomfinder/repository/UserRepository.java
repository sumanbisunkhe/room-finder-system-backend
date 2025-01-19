package com.roomfinder.repository;

import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
    @Query("SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)")
    User findByUsernameIgnoreCase(@Param("username") String username);
    User findByEmail(String email);
    List<User> findAllByRole(UserRole role);
    List<User> findAllByRoleIn(List<UserRole> roles);
}
