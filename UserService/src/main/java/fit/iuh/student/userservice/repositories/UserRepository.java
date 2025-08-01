package fit.iuh.student.userservice.repositories;

import fit.iuh.student.userservice.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}