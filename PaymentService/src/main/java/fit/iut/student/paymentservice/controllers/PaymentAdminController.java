package fit.iut.student.paymentservice.controllers;

import fit.iut.student.paymentservice.dtos.admin.RevenueByDateResponse;
import fit.iut.student.paymentservice.dtos.admin.RevenueStatisticsResponse;
import fit.iut.student.paymentservice.entities.Payment;
import fit.iut.student.paymentservice.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payments/admin")
@RequiredArgsConstructor
@Slf4j
public class PaymentAdminController {

    private final PaymentRepository paymentRepository;

    /**
     * Get revenue statistics by date range
     * Admin only endpoint
     */
    @GetMapping("/revenue-statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueStatisticsResponse> getRevenueStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin request: Get revenue statistics from {} to {}", startDate, endDate);
        
        Long totalRevenue = paymentRepository.sumRevenueBetweenDates(startDate, endDate);
        Long paymentCount = paymentRepository.countPaidPayments(startDate, endDate);
        Double avgAmount = paymentRepository.avgPaymentAmount(startDate, endDate);
        
        RevenueStatisticsResponse response = RevenueStatisticsResponse.builder()
                .totalRevenue(totalRevenue != null ? totalRevenue : 0L)
                .paymentCount(paymentCount != null ? paymentCount : 0L)
                .averagePaymentAmount(avgAmount != null ? avgAmount : 0.0)
                .build();
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get revenue by date (daily breakdown)
     * Admin only endpoint
     */
    @GetMapping("/by-date")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<RevenueByDateResponse>> getRevenueByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin request: Get revenue by date from {} to {}", startDate, endDate);
        
        List<Object[]> results = paymentRepository.getRevenueByDate(startDate, endDate);
        
        List<RevenueByDateResponse> response = results.stream()
                .map(row -> RevenueByDateResponse.builder()
                        .date(row[0] instanceof Date ? ((Date) row[0]).toLocalDate() : (LocalDate) row[0])
                        .revenue(((Number) row[1]).longValue())
                        .count(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get payments by appointment IDs
     * Admin only endpoint
     */
    @PostMapping("/by-appointments")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaymentsByAppointments(@RequestBody List<String> appointmentIds) {
        log.info("Admin request: Get payments by appointment IDs, count: {}", appointmentIds.size());
        List<Payment> payments = paymentRepository.findPaidPaymentsByAppointmentIds(appointmentIds);
        return ResponseEntity.ok(payments);
    }

    /**
     * Get all PAID payments within date range
     * Admin only endpoint
     */
    @GetMapping("/paid")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Payment>> getPaidPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate
    ) {
        log.info("Admin request: Get paid payments from {} to {}", startDate, endDate);
        List<Payment> payments = paymentRepository.findPaidPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }
}
