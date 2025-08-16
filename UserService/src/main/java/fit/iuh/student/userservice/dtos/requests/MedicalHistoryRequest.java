package fit.iuh.student.userservice.dtos.requests;

import fit.iuh.student.userservice.entities.Patient;
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

    private String condition;

    private LocalDate diagnosisDate;

    private String notes;
}
