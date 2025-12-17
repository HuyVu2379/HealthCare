package fit.iuh.student.adminservice.controllers;

import fit.iuh.student.adminservice.dtos.revenue.*;
import fit.iuh.student.adminservice.services.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/revenue")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminRevenueController {

    private final AdminRevenueService revenueService;

    /**
     * Get revenue overview (total revenue, appointments, average)
     */
    @GetMapping("/overview")
    public ResponseEntity<RevenueOverviewResponse> getOverview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin - Get revenue overview from {} to {}", startDate, endDate);
        RevenueOverviewResponse response = revenueService.getRevenueOverview(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get revenue by time (daily breakdown)
     */
    @GetMapping("/by-time")
    public ResponseEntity<List<RevenueByDateResponse>> getRevenueByTime(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin - Get revenue by time from {} to {}", startDate, endDate);
        List<RevenueByDateResponse> response = revenueService.getRevenueByTime(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get revenue by doctor with pagination
     */
    @GetMapping("/by-doctor")
    public ResponseEntity<Page<DoctorRevenueResponse>> getRevenueByDoctor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            Pageable pageable
    ) {
        log.info("Admin - Get revenue by doctor from {} to {}", startDate, endDate);
        Page<DoctorRevenueResponse> response = revenueService.getRevenueByDoctor(startDate, endDate, pageable);
        return ResponseEntity.ok(response);
    }

    /**
     * Get revenue by specialty
     */
    @GetMapping("/by-specialty")
    public ResponseEntity<List<SpecialtyRevenueResponse>> getRevenueBySpecialty(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin - Get revenue by specialty from {} to {}", startDate, endDate);
        List<SpecialtyRevenueResponse> response = revenueService.getRevenueBySpecialty(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get revenue by service type (consultation type)
     */
    @GetMapping("/by-service-type")
    public ResponseEntity<List<ServiceTypeRevenueResponse>> getRevenueByServiceType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin - Get revenue by service type from {} to {}", startDate, endDate);
        List<ServiceTypeRevenueResponse> response = revenueService.getRevenueByServiceType(startDate, endDate);
        return ResponseEntity.ok(response);
    }

    /**
     * Get top performing doctors by revenue
     */
    @GetMapping("/top-performers")
    public ResponseEntity<List<DoctorRevenueResponse>> getTopPerformers(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "10") int limit
    ) {
        log.info("Admin - Get top {} performers from {} to {}", limit, startDate, endDate);
        List<DoctorRevenueResponse> response = revenueService.getTopPerformers(startDate, endDate, limit);
        return ResponseEntity.ok(response);
    }
}
