package fit.iuh.student.adminservice.dtos.dashboard;

import fit.iuh.student.adminservice.dtos.revenue.DoctorRevenueResponse;
import fit.iuh.student.adminservice.dtos.revenue.RevenueByDateResponse;
import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private DashboardStatistics statistics;
    private DashboardCharts charts;
    private RecentActivities recentActivities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStatistics {
        private Long totalRevenueThisMonth;
        private Long totalAppointmentsThisMonth;
        private Long totalActiveUsers;
        private Double growthRate; // % compared to last month
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardCharts {
        private List<RevenueByDateResponse> revenueTrend; // Last 30 days
        private Map<String, Long> appointmentsByStatus;
        private List<DoctorRevenueResponse> topDoctors; // Top 5
        private Map<String, Long> revenueByServiceType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecentActivities {
        private List<Object> recentUsers;
        private List<Object> recentAppointments;
        private List<Object> recentPayments;
    }
}
