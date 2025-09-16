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
public class CreateHealthMetricPanelRequest {
    private String patientId;
    private String recordId; // optional
    private Date measuredAt;
    private List<MetricItem> metrics;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetricItem {
        private String name;
        private double value;
        private String unit;
    }
}


