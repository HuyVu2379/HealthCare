package fit.iuh.student.adminservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Feign Client for PaymentService
 * Communicates with PaymentService to fetch payment and revenue information
 */
@FeignClient(name = "payment-service")
public interface PaymentClient {

    /**
     * Get revenue statistics by date range (will be implemented in PaymentService)
     */
    @GetMapping("/api/v1/payments/admin/revenue-statistics")
    ResponseEntity<Object> getRevenueStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    );

    /**
     * Get revenue by date (daily breakdown) (will be implemented in PaymentService)
     */
    @GetMapping("/api/v1/payments/admin/by-date")
    ResponseEntity<List<Object>> getRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    );

    /**
     * Get payments by appointment IDs (will be implemented in PaymentService)
     */
    @PostMapping("/api/v1/payments/admin/by-appointments")
    ResponseEntity<List<Object>> getPaymentsByAppointments(@RequestBody List<String> appointmentIds);

    /**
     * Get all PAID payments within date range (will be implemented in PaymentService)
     */
    @GetMapping("/api/v1/payments/admin/paid")
    ResponseEntity<List<Object>> getPaidPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    );
}
