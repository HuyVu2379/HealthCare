package fit.iuh.student.userservice.clients.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricClientResponse {
    private String metricId;
    private String patientId;
    private String metricName;
    private double metricValue;
    private String unit;
}