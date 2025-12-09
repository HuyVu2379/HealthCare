package fit.iut.student.paymentservice.dtos.admin;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueStatisticsResponse {
    private Long totalRevenue;
    private Long paymentCount;
    private Double averagePaymentAmount;
}
