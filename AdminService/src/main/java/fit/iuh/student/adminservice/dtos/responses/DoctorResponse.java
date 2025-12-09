package fit.iuh.student.adminservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private String userId;
    private String fullName;
    private String email;
    private String phone;
    private String specialty;
    private Integer experienceYears;
    private Double rating;
    private Integer examinationFee;
}
