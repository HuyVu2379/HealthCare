package fit.iuh.student.adminservice.dtos.revenue;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRevenueResponse {
    private String doctorId;
    private String doctorName;
    private String specialty;
    private Long totalRevenue;
    private Long appointmentCount;
    private Double rating;
}
