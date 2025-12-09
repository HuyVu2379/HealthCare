package fit.iuh.student.adminservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponse {
    private Long totalRevenue;
    private Long totalPayments;
    private Double averagePaymentAmount;
    private Long growthRate; // Percentage growth compared to previous period
}
