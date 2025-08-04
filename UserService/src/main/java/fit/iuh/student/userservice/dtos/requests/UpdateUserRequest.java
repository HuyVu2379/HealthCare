package fit.iuh.student.userservice.dtos.requests;

import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UpdateUserRequest {
    private String userId;

    private String fullname;

    private Gender gender;

    private LocalDate dob;

    private String phone;

    private String address;

    private Role role;
}
