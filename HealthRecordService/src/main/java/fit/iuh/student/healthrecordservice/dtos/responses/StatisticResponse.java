package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatisticResponse {
    private double changePercent;
    private boolean isIncrease;
    private List<HeathMetricDTO> currentMetric;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HeathMetricDTO {
        private String metricId;
        private String metricName;
        private double metricValue;
        private String unit;
        private Date measuredAt;
    }
}
