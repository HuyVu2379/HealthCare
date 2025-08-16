package fit.iuh.student.userservice.dtos.requests;

import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    private String userId;

    private String fullName;

    private Gender gender;

    private LocalDate dob;

    private String phone;

    private String address;

    private Role role;
}
