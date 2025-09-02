package fit.iuh.student.healthrecordservice.dtos.requests;

import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ImportHealthMetricsRequest {
    private List<HealthMetricData> healthMetrics;
    @Data
    public static class HealthMetricData {
        private String patientId;
        private String metricName;
        private int metricValue;
        private String unit;
        private String recordId;
        private Date measuredAt;
    }
}
