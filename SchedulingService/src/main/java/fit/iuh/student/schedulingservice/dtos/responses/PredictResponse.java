package fit.iuh.student.schedulingservice.dtos.responses;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictResponse {
    private String predictId;
    private String patientId;
    private int state;
    private String recommended;
    private List<HealthMetricResponse> healthMetrics;
}
