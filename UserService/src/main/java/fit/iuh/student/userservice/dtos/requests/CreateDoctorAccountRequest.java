package fit.iuh.student.userservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDoctorAccountRequest {
    private String fullName;
    private String email;
    private String phone;
    private String specialty;
    private String password;
}
