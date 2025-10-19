package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HealthMetricResponseWithBatch {
    private List<HealthMetricResponse> healthMetrics;
    private LocalDateTime measuredAt;
}
