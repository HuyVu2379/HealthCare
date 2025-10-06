package fit.iuh.student.healthrecordservice.dtos.responses;

import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HealthMetricResponse {
    private String metricId;
    private String patientId;
    private String metricName;
    private double metricValue;
    private String unit;
    private String medicalRecordId = null;
    private Date measuredAt;
}
