package fit.iuh.student.userservice.dtos.requests;

import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for user registration request
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    private String fullName;
    private String email;
    private String password;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private Gender gender;
    private Role role;
}