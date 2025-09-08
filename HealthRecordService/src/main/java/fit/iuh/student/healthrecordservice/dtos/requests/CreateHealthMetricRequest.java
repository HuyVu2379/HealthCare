package fit.iuh.student.healthrecordservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

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
    private List<String> imageAttachments;
}
