package fit.iuh.student.adminservice.dtos.revenue;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueOverviewResponse {
    private Long totalRevenue;
    private Long totalAppointments;
    private Double averagePaymentAmount;
    private Long completedAppointments;
}
