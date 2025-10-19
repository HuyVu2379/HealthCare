package fit.iuh.student.schedulingservice.clients.dtos;

import fit.iuh.student.schedulingservice.dtos.responses.HealthMetricResponse;
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
