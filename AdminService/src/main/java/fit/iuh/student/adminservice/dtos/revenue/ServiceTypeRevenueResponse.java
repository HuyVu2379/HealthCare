package fit.iuh.student.adminservice.dtos.revenue;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceTypeRevenueResponse {
    private String serviceType;
    private Long totalRevenue;
    private Long appointmentCount;
    private Double percentage;
}
