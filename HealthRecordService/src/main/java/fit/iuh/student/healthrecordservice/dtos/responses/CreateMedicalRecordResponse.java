package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordResponse {
    private String patientId;
    private String metricName;
    private int metricValue;
    private String unit;
    private String recordId;
    private Date measuredAt;
}
