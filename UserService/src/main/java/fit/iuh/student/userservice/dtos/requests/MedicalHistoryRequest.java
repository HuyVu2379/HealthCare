package fit.iuh.student.userservice.dtos.requests;

import fit.iuh.student.userservice.enums.StatusHealth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistoryRequest {
    private String userId;

    private String doctorId;

    private String serviceName;

    private LocalDate diagnosisDate;

    private String notes;

    private String diagnosis;

    private int stage;

    private StatusHealth statusHealth;
}
