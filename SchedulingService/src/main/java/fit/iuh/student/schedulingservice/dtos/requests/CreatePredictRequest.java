package fit.iuh.student.schedulingservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePredictRequest {
    private String patientId;
    private int state;
    private String recommended;
    private List<CreateHealthMetricRequest> healthMetrics;
}
