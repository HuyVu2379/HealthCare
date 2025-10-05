package fit.iuh.student.schedulingservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateHealthMetricRequest {
    private String patientId;
    private String metricName;
    private double metricValue;
    private String unit;
    private String recordId;
    private Date measuredAt;
}
