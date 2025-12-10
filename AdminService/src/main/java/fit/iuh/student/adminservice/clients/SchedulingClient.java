package fit.iuh.student.adminservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Feign Client for SchedulingService
 * Communicates with SchedulingService to fetch appointment information
 */
@FeignClient(name = "SchedulingService")
public interface SchedulingClient {

    /**
     * Get appointment statistics by date range (will be implemented in SchedulingService)
     */
    @GetMapping("/api/v1/appointments/admin/statistics")
    ResponseEntity<Object> getStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    /**
     * Get appointments by IDs (will be implemented in SchedulingService)
     */
    @PostMapping("/api/v1/appointments/admin/by-ids")
    ResponseEntity<List<Object>> getAppointmentsByIds(@RequestBody List<String> appointmentIds);

    /**
     * Get statistics by consultation type (will be implemented in SchedulingService)
     */
    @GetMapping("/api/v1/appointments/admin/by-consultation-type")
    ResponseEntity<List<Object>> getStatsByConsultationType(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );

    /**
     * Get completed appointments count by doctor (will be implemented in SchedulingService)
     */
    @GetMapping("/api/v1/appointments/admin/by-doctor")
    ResponseEntity<List<Object>> getCompletedByDoctor(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    );
}
