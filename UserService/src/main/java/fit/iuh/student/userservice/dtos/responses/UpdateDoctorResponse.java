package fit.iuh.student.userservice.dtos.responses;

import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorResponse {
    private String userId;
    private String email;
    private String fullname;
    private Gender gender;
    private LocalDate dob;
    private String phone;
    private String address;
    private String avatarUrl;
    private Role role;

    // Doctor-specific fields
    private String specialty;
    private Integer experienceYears;
    private String bio;
    private List<String> certifications;
}
