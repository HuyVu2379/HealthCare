package fit.iuh.student.schedulingservice.dtos.admin;

import lombok.*;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatisticsResponse {
    private Long totalAppointments;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> appointmentsByType;
}
