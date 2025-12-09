package fit.iuh.student.adminservice.controllers;

import fit.iuh.student.adminservice.dtos.dashboard.AdminDashboardResponse;
import fit.iuh.student.adminservice.services.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/dashboard")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    /**
     * Get complete admin dashboard data
     * Includes: statistics, charts, recent activities
     */
    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard() {
        log.info("Admin - Get dashboard data");
        AdminDashboardResponse response = dashboardService.getDashboardData();
        return ResponseEntity.ok(response);
    }
}
