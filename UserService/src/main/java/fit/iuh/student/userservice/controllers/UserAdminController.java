package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.admin.UpdateUserStatusRequest;
import fit.iuh.student.userservice.dtos.admin.UserAdminResponse;
import fit.iuh.student.userservice.dtos.admin.UserStatisticsResponse;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import fit.iuh.student.userservice.services.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/admin")
@RequiredArgsConstructor
@Slf4j
public class UserAdminController {

    private final UserAdminService userAdminService;

    /**
     * Get users with filters and pagination
     * Admin only endpoint
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserAdminResponse>> getUsersWithFilters(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String search,
            Pageable pageable
    ) {
        log.info("Admin request: Get users with filters - role: {}, status: {}, search: {}", role, status, search);
        Page<UserAdminResponse> users = userAdminService.getUsersWithFilters(role, status, search, pageable);
        return ResponseEntity.ok(users);
    }

    /**
     * Get user details by ID
     * Admin only endpoint
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> getUserDetails(@PathVariable String userId) {
        log.info("Admin request: Get user details for userId: {}", userId);
        UserAdminResponse user = userAdminService.getUserDetails(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Update user status
     * Admin only endpoint
     */
    @PutMapping("/{userId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserAdminResponse> updateUserStatus(
            @PathVariable String userId,
            @RequestBody UpdateUserStatusRequest request
    ) {
        log.info("Admin request: Update user status for userId: {} to {}", userId, request.getNewStatus());
        UserAdminResponse user = userAdminService.updateUserStatus(userId, request);
        return ResponseEntity.ok(user);
    }

    /**
     * Get user statistics
     * Admin only endpoint
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserStatisticsResponse> getUserStatistics() {
        log.info("Admin request: Get user statistics");
        UserStatisticsResponse statistics = userAdminService.getUserStatistics();
        return ResponseEntity.ok(statistics);
    }
}
