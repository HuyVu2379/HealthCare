package fit.iuh.student.userservice.dtos.responses;

import com.fasterxml.jackson.annotation.JsonFormat;
import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetPatientResponse {
    private String userId;
    private String fullName;
    private String email;
    private Gender gender;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy/MM/dd")
    private LocalDate dob;
    private String phone;
    private String address;
    private String avatarUrl;
    private Role role;
    private Status status;
    private int height; // in cm
    private double weight; // in kg
    private String bloodType; // e.g., A+, O-, etc.
    private double bmi; // Body Mass Index
}
