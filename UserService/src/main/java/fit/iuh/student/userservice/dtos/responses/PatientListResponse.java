package fit.iuh.student.userservice.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import fit.iuh.student.userservice.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientListResponse {
    private String patientId;
    private String fullName;
    private int age;
    private Gender gender;
    @JsonFormat(pattern = "dd/MM/yyyy", timezone = "Asia/Ho_Chi_Minh")
    private LocalDate lastVisitDate;
}
