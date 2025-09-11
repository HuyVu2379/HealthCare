package fit.iuh.student.userservice.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import fit.iuh.student.userservice.clients.dtos.HealthMetricClientResponse;
import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.StatusHealth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponse {
    private String patientId;
    private String fullName;
    private int age;
    private Gender gender;
    private HealthMetricClientResponse healthMetric;
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    private Date lastDiagnosisDate;
    private StatusHealth statusHealth;
}
