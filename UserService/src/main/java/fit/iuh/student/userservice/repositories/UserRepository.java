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
    @Query(value = "SELECT u.* FROM users u " +
           "LEFT JOIN doctors d ON u.user_id = d.user_id " +
           "LEFT JOIN patients p ON u.user_id = p.user_id " +
           "WHERE (:role IS NULL OR u.role = CAST(:role AS VARCHAR)) AND " +
           "(:status IS NULL OR u.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "(u.full_name IS NOT NULL AND LOWER(CAST(u.full_name AS VARCHAR)) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))) OR " +
           "(u.email IS NOT NULL AND LOWER(CAST(u.email AS VARCHAR)) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))) OR " +
           "(u.phone IS NOT NULL AND CAST(u.phone AS VARCHAR) LIKE CONCAT('%', CAST(:search AS VARCHAR), '%')))",
           nativeQuery = true,
           countQuery = "SELECT COUNT(*) FROM users u " +
           "LEFT JOIN doctors d ON u.user_id = d.user_id " +
           "LEFT JOIN patients p ON u.user_id = p.user_id " +
           "WHERE (:role IS NULL OR u.role = CAST(:role AS VARCHAR)) AND " +
           "(:status IS NULL OR u.status = CAST(:status AS VARCHAR)) AND " +
           "(:search IS NULL OR :search = '' OR " +
           "(u.full_name IS NOT NULL AND LOWER(CAST(u.full_name AS VARCHAR)) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))) OR " +
           "(u.email IS NOT NULL AND LOWER(CAST(u.email AS VARCHAR)) LIKE LOWER(CONCAT('%', CAST(:search AS VARCHAR), '%'))) OR " +
           "(u.phone IS NOT NULL AND CAST(u.phone AS VARCHAR) LIKE CONCAT('%', CAST(:search AS VARCHAR), '%')))")
    Page<User> findUsersWithFilters(
            @Param("role") String role,
            @Param("status") String status,
            @Param("search") String search,
            Pageable pageable
    );
}