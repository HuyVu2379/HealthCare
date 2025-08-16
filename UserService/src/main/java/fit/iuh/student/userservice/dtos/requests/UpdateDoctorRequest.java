package fit.iuh.student.userservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorRequest {
    private String userId;
    private String specialty;
    private Integer experienceYears;
    private String bio;
}
