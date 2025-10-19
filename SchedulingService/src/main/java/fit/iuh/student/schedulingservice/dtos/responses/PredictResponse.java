package fit.iuh.student.schedulingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictResponse {
    private String predictId;
    private String patientId;
    private int stage;
    private List<String> recommendations;
    private double confidence;
    private List<HealthMetricResponse> healthMetrics;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
