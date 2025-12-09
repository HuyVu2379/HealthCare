package fit.iuh.student.adminservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatisticsResponse {
    private Long totalAppointments;
    private Map<String, Long> appointmentsByStatus;
    private Map<String, Long> appointmentsByConsultationType;
}
