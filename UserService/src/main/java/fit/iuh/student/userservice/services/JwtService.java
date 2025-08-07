package fit.iuh.student.userservice.services;

import org.springframework.security.core.userdetails.UserDetails;

import java.util.Map;
import java.util.function.Function;

/**
 * Service interface for JWT operations
 */
public interface JwtService {
    /**
     * Extract username from token
     * @param token JWT token
     * @return username
     */
    String extractUsername(String token);

    /**
     * Generate token for user
     * @param userDetails user details
     * @return JWT token
     */
    String generateToken(UserDetails userDetails);

    /**
     * Generate token with extra claims
     * @param extraClaims additional claims to include in token
     * @param userDetails user details
     * @return JWT token
     */
    String generateToken(Map<String, Object> extraClaims, UserDetails userDetails);

    /**
     * Validate token
     * @param token JWT token
     * @param userDetails user details
     * @return true if token is valid, false otherwise
     */
    boolean isTokenValid(String token, UserDetails userDetails);
    
    /**
     * Generate refresh token
     * @param userDetails user details
     * @return refresh token
     */
    String generateRefreshToken(UserDetails userDetails);

    void blacklistToken(String token);

    boolean isBlacklisted(String token);
}