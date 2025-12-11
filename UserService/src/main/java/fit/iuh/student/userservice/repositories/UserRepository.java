package fit.iuh.student.userservice.repositories;

import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for User entity
 */
@Repository
public interface UserRepository extends JpaRepository<User, String> {

    /**
     * Find user by email
     * @param email user email
     * @return optional user
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * @param email user email
     * @return true if user exists, false otherwise
     */
    boolean existsByEmail(String email);

    // ========== ADMIN QUERIES ==========

    /**
     * Count users by role
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.role = :role")
    Long countByRole(@Param("role") Role role);

    /**
     * Count users by status
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.status = :status")
    Long countByStatus(@Param("status") Status status);

    /**
     * Get user statistics by role
     */
    @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
    List<Object[]> getUserStatisticsByRole();

    /**
     * Get user statistics by status
     */
    @Query("SELECT u.status, COUNT(u) FROM User u GROUP BY u.status")
    List<Object[]> getUserStatisticsByStatus();

    /**
     * Find users with filters and pagination
     */
    @Query(value = "SELECT DISTINCT u.*, " +
           "CASE WHEN d.user_id IS NOT NULL THEN 1 " +
           "WHEN p.user_id IS NOT NULL THEN 2 " +
           "ELSE 0 END AS clazz_ " +
           "FROM users u " +
           "LEFT JOIN doctors d ON u.user_id = d.user_id " +
           "LEFT JOIN patients p ON u.user_id = p.user_id " +
           "WHERE (:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "(u.full_name IS NOT NULL AND LOWER(u.full_name::text) LIKE LOWER('%' || :search || '%')) OR " +
           "(u.email IS NOT NULL AND LOWER(u.email::text) LIKE LOWER('%' || :search || '%')) OR " +
           "(u.phone IS NOT NULL AND u.phone::text LIKE '%' || :search || '%'))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(DISTINCT u.user_id) FROM users u " +
           "LEFT JOIN doctors d ON u.user_id = d.user_id " +
           "LEFT JOIN patients p ON u.user_id = p.user_id " +
           "WHERE (:role IS NULL OR u.role = :role) AND " +
           "(:status IS NULL OR u.status = :status) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "(u.full_name IS NOT NULL AND LOWER(u.full_name::text) LIKE LOWER('%' || :search || '%')) OR " +
           "(u.email IS NOT NULL AND LOWER(u.email::text) LIKE LOWER('%' || :search || '%')) OR " +
           "(u.phone IS NOT NULL AND u.phone::text LIKE '%' || :search || '%'))")
    Page<User> findUsersWithFilters(
            @Param("role") String role,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}