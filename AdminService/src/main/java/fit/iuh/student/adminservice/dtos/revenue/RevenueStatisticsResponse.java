package fit.iuh.student.adminservice.dtos.revenue;

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
    private Long paymentCount;
    private Double averagePaymentAmount;
}
