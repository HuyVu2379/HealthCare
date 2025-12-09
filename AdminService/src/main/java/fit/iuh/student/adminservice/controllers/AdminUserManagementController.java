package fit.iuh.student.adminservice.controllers;

import fit.iuh.student.adminservice.clients.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserManagementController {

    private final UserClient userClient;

    /**
     * Get users with filters and pagination
     * Forwards request to UserService
     */
    @GetMapping
    public ResponseEntity<Page<Object>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        log.info("Admin - Get users with filters: role={}, status={}, search={}", role, status, search);
        return userClient.getUsersWithFilters(role, status, search, pageable);
    }

    /**
     * Get user details by ID
     * Forwards request to UserService
     */
    @GetMapping("/{userId}")
    public ResponseEntity<Object> getUserDetails(@PathVariable String userId) {
        log.info("Admin - Get user details for userId: {}", userId);
        return userClient.getUserDetails(userId);
    }

    /**
     * Update user status (ACTIVE/INACTIVE/BLOCKED)
     * Forwards request to UserService
     */
    @PutMapping("/{userId}/status")
    public ResponseEntity<Object> updateUserStatus(
            @PathVariable String userId,
            @RequestBody Object request
    ) {
        log.info("Admin - Update user status for userId: {}", userId);
        return userClient.updateUserStatus(userId, request);
    }

    /**
     * Get user statistics (counts by role, status, etc.)
     * Forwards request to UserService
     */
    @GetMapping("/statistics")
    public ResponseEntity<Object> getUserStatistics() {
        log.info("Admin - Get user statistics");
        return userClient.getUserStatistics();
    }
}
