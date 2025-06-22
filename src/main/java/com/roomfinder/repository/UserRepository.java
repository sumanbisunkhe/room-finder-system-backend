package com.roomfinder.repository;

import com.roomfinder.entity.User;
import com.roomfinder.enums.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Page<User> findAllByRole(UserRole role, Pageable pageable);
    Page<User> findAllByIsActive( boolean isActive, Pageable pageable);



    Page<User> findAllByRoleIn(List<UserRole> roles, Pageable pageable);

    boolean existsByUsername(String username);

    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<User> searchByUsernameOrEmail(@Param("keyword") String keyword, Pageable pageable);

    long countByIsActive(boolean isActive);

    long countByRole(UserRole role);

    // Monthly growth trends
    @Query(value = """
            SELECT 
                TO_CHAR(u.created_at, 'YYYY-MM') AS period,
                COUNT(u.id) AS user_count,
                TO_CHAR(u.created_at, 'TMMonth YYYY') AS period_label
            FROM users u
            GROUP BY TO_CHAR(u.created_at, 'YYYY-MM'), TO_CHAR(u.created_at, 'TMMonth YYYY')
            ORDER BY period
            """, nativeQuery = true)
    List<Object[]> findMonthlyGrowth();

    @Query(value = """
            SELECT 
                TO_CHAR(u.created_at, 'YYYY-MM-DD') AS period,
                COUNT(u.id) AS user_count,
                TO_CHAR(u.created_at, 'DD Mon YYYY') AS period_label
            FROM users u
            GROUP BY TO_CHAR(u.created_at, 'YYYY-MM-DD'), TO_CHAR(u.created_at, 'DD Mon YYYY')
            ORDER BY period
            """, nativeQuery = true)
    List<Object[]> findDailyGrowth();
}
