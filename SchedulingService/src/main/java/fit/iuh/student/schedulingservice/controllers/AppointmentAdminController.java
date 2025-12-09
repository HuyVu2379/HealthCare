package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.admin.AppointmentStatisticsResponse;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/appointments/admin")
@RequiredArgsConstructor
@Slf4j
public class AppointmentAdminController {

    private final AppointmentRepository appointmentRepository;

    /**
     * Get appointment statistics by date range
     * Admin only endpoint
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AppointmentStatisticsResponse> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Admin request: Get appointment statistics from {} to {}", startDate, endDate);
        
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);
        
        // Get total count
        Long totalAppointments = appointmentRepository.countAppointmentsByDateRange(sqlStartDate, sqlEndDate);
        
        // Get counts by status
        List<Object[]> statusResults = appointmentRepository.countByStatus(sqlStartDate, sqlEndDate);
        Map<String, Long> byStatus = new HashMap<>();
        for (Object[] row : statusResults) {
            byStatus.put(((AppointmentStatus) row[0]).name(), ((Number) row[1]).longValue());
        }
        
        // Get counts by type
        List<Object[]> typeResults = appointmentRepository.countByConsultationType(sqlStartDate, sqlEndDate);
        Map<String, Long> byType = new HashMap<>();
        for (Object[] row : typeResults) {
            byType.put(((ConsultationType) row[0]).name(), ((Number) row[1]).longValue());
        }
        
        AppointmentStatisticsResponse response = AppointmentStatisticsResponse.builder()
                .totalAppointments(totalAppointments != null ? totalAppointments : 0L)
                .appointmentsByStatus(byStatus)
                .appointmentsByType(byType)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get appointments by IDs
     * Admin only endpoint
     */
    @PostMapping("/by-ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Appointment>> getAppointmentsByIds(@RequestBody List<String> appointmentIds) {
        log.info("Admin request: Get appointments by IDs, count: {}", appointmentIds.size());
        List<Appointment> appointments = appointmentRepository.findByAppointmentIdIn(appointmentIds);
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get statistics by consultation type
     * Admin only endpoint
     */
    @GetMapping("/by-consultation-type")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStatsByConsultationType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Admin request: Get stats by consultation type from {} to {}", startDate, endDate);
        
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);
        
        List<Object[]> results = appointmentRepository.countByConsultationType(sqlStartDate, sqlEndDate);
        Map<String, Long> response = new HashMap<>();
        
        for (Object[] row : results) {
            response.put(((ConsultationType) row[0]).name(), ((Number) row[1]).longValue());
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get completed appointments count by doctor
     * Admin only endpoint
     */
    @GetMapping("/by-doctor")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getCompletedByDoctor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        log.info("Admin request: Get completed appointments by doctor from {} to {}", startDate, endDate);
        
        Date sqlStartDate = Date.valueOf(startDate);
        Date sqlEndDate = Date.valueOf(endDate);
        
        List<Object[]> results = appointmentRepository.countCompletedByDoctor(sqlStartDate, sqlEndDate);
        Map<String, Long> response = new HashMap<>();
        
        for (Object[] row : results) {
            response.put((String) row[0], ((Number) row[1]).longValue());
        }
        
        return ResponseEntity.ok(response);
    }
}
