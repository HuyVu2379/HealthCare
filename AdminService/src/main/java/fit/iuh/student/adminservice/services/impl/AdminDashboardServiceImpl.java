package fit.iuh.student.adminservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.student.adminservice.clients.PaymentClient;
import fit.iuh.student.adminservice.clients.SchedulingClient;
import fit.iuh.student.adminservice.clients.UserClient;
import fit.iuh.student.adminservice.dtos.dashboard.AdminDashboardResponse;
import fit.iuh.student.adminservice.dtos.revenue.DoctorRevenueResponse;
import fit.iuh.student.adminservice.dtos.revenue.RevenueByDateResponse;
import fit.iuh.student.adminservice.services.AdminDashboardService;
import fit.iuh.student.adminservice.services.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final PaymentClient paymentClient;
    private final SchedulingClient schedulingClient;
    private final UserClient userClient;
    private final AdminRevenueService revenueService;
    private final ObjectMapper objectMapper;

    /**
     * Helper method to safely convert Number to Long
     * Handles both Integer and Long types from JSON deserialization
     */
    private Long toLong(Object value) {
        if (value == null) {
            return 0L;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    @Override
    public AdminDashboardResponse getDashboardData() {
        log.info("Getting admin dashboard data");

        try {
            // Calculate date ranges
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime startOfThisMonth = YearMonth.now().atDay(1).atStartOfDay();
            LocalDateTime endOfThisMonth = YearMonth.now().atEndOfMonth().atTime(23, 59, 59);
            LocalDateTime startOfLastMonth = YearMonth.now().minusMonths(1).atDay(1).atStartOfDay();
            LocalDateTime endOfLastMonth = YearMonth.now().minusMonths(1).atEndOfMonth().atTime(23, 59, 59);
            LocalDateTime last30Days = now.minusDays(30);

            // Get statistics for this month
            ResponseEntity<Object> thisMonthRevenueResponse = paymentClient.getRevenueStatistics(startOfThisMonth, endOfThisMonth);
            @SuppressWarnings("unchecked")
            Map<String, Object> thisMonthRevenue = Objects.requireNonNull(
                    objectMapper.convertValue(thisMonthRevenueResponse.getBody(), Map.class)
            );

            Long totalRevenueThisMonth = toLong(thisMonthRevenue.getOrDefault("totalRevenue", 0));

            // Get statistics for last month (for growth calculation)
            ResponseEntity<Object> lastMonthRevenueResponse = paymentClient.getRevenueStatistics(startOfLastMonth, endOfLastMonth);
            @SuppressWarnings("unchecked")
            Map<String, Object> lastMonthRevenue = Objects.requireNonNull(
                    objectMapper.convertValue(lastMonthRevenueResponse.getBody(), Map.class)
            );
            Long totalRevenueLastMonth = toLong(lastMonthRevenue.getOrDefault("totalRevenue", 0));

            // Calculate growth rate
            double growthRate = 0.0;
            if (totalRevenueLastMonth > 0) {
                growthRate = ((double) (totalRevenueThisMonth - totalRevenueLastMonth) * 100.0) / totalRevenueLastMonth;
            }

            // Get appointment statistics this month
            LocalDate startLocalDate = startOfThisMonth.toLocalDate();
            LocalDate endLocalDate = endOfThisMonth.toLocalDate();
            ResponseEntity<Object> appointmentStatsResponse = schedulingClient.getStatistics(startLocalDate, endLocalDate);
            @SuppressWarnings("unchecked")
            Map<String, Object> appointmentStats = Objects.requireNonNull(
                    objectMapper.convertValue(appointmentStatsResponse.getBody(), Map.class)
            );

            Long totalAppointmentsThisMonth = toLong(appointmentStats.getOrDefault("totalAppointments", 0));
            @SuppressWarnings("unchecked")
            Map<String, Object> appointmentsByStatusRaw = (Map<String, Object>) appointmentStats.get("appointmentsByStatus");
            Map<String, Long> appointmentsByStatus = appointmentsByStatusRaw != null ?
                    appointmentsByStatusRaw.entrySet().stream()
                            .collect(java.util.stream.Collectors.toMap(
                                    Map.Entry::getKey,
                                    e -> toLong(e.getValue())
                            )) : Collections.emptyMap();

            // Get user statistics
            ResponseEntity<Object> userStatsResponse = userClient.getUserStatistics();
            @SuppressWarnings("unchecked")
            Map<String, Object> userStats = Objects.requireNonNull(
                    objectMapper.convertValue(userStatsResponse.getBody(), Map.class)
            );
            Long activeUsers = toLong(userStats.getOrDefault("activeUsers", 0));

            // Build statistics
            AdminDashboardResponse.DashboardStatistics statistics = AdminDashboardResponse.DashboardStatistics.builder()
                    .totalRevenueThisMonth(totalRevenueThisMonth)
                    .totalAppointmentsThisMonth(totalAppointmentsThisMonth)
                    .totalActiveUsers(activeUsers)
                    .growthRate(growthRate)
                    .build();

            // Get chart data
            // Revenue trend for last 30 days
            List<RevenueByDateResponse> revenueTrend = revenueService.getRevenueByTime(last30Days, now);

            // Top 5 doctors by revenue this month
            List<DoctorRevenueResponse> topDoctors = revenueService.getTopPerformers(startOfThisMonth, endOfThisMonth, 5);

            // Revenue by service type this month
            Map<String, Long> revenueByServiceType = revenueService.getRevenueByServiceType(startOfThisMonth, endOfThisMonth)
                    .stream()
                    .collect(java.util.stream.Collectors.toMap(
                            fit.iuh.student.adminservice.dtos.revenue.ServiceTypeRevenueResponse::getServiceType,
                            fit.iuh.student.adminservice.dtos.revenue.ServiceTypeRevenueResponse::getTotalRevenue
                    ));

            AdminDashboardResponse.DashboardCharts charts = AdminDashboardResponse.DashboardCharts.builder()
                    .revenueTrend(revenueTrend)
                    .appointmentsByStatus(appointmentsByStatus != null ? appointmentsByStatus : Collections.emptyMap())
                    .topDoctors(topDoctors)
                    .revenueByServiceType(revenueByServiceType)
                    .build();

            // Recent activities (simplified - just empty lists for now)
            AdminDashboardResponse.RecentActivities recentActivities = AdminDashboardResponse.RecentActivities.builder()
                    .recentUsers(Collections.emptyList())
                    .recentAppointments(Collections.emptyList())
                    .recentPayments(Collections.emptyList())
                    .build();

            return AdminDashboardResponse.builder()
                    .statistics(statistics)
                    .charts(charts)
                    .recentActivities(recentActivities)
                    .build();

        } catch (Exception e) {
            log.error("Error getting dashboard data", e);
            throw new RuntimeException("Failed to get dashboard data: " + e.getMessage());
        }
    }
}
