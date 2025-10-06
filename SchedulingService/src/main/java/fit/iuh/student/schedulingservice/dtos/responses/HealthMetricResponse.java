package fit.iuh.student.schedulingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthMetricResponse {
    private String metricId;
    private String patientId;
    private String metricName;
    private double metricValue;
    private String unit;
    private String medicalRecordId = null;
    private Date measuredAt;
}
