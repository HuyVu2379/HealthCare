package fit.iuh.student.adminservice.dtos.revenue;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyRevenueResponse {
    private String specialty;
    private Long totalRevenue;
    private Long appointmentCount;
    private Double percentage;
}
