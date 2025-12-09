package fit.iuh.student.adminservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Feign Client for UserService
 * Communicates with UserService to fetch user and doctor information
 */
@FeignClient(name = "UserService")
public interface UserClient {

    /**
     * Get users with filters and pagination (will be implemented in UserService)
     */
    @GetMapping("/api/v1/users/admin")
    ResponseEntity<Page<Object>> getUsersWithFilters(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable
    );

    /**
     * Get user details by ID (will be implemented in UserService)
     */
    @GetMapping("/api/v1/users/admin/{userId}")
    ResponseEntity<Object> getUserDetails(@PathVariable String userId);

    /**
     * Update user status (will be implemented in UserService)
     */
    @PutMapping("/api/v1/users/admin/{userId}/status")
    ResponseEntity<Object> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Object request
    );

    /**
     * Get user statistics (will be implemented in UserService)
     */
    @GetMapping("/api/v1/users/admin/statistics")
    ResponseEntity<Object> getUserStatistics();

    /**
     * Get doctor by ID
     */
    @GetMapping("/api/v1/doctors/getDoctorForClient/{doctorId}")
    ResponseEntity<Object> getDoctorById(@PathVariable String doctorId);

    /**
     * Get multiple doctors by IDs (will be implemented in UserService)
     */
    @PostMapping("/api/v1/doctors/admin/by-ids")
    ResponseEntity<List<Object>> getDoctorsByIds(@RequestBody List<String> doctorIds);
}
