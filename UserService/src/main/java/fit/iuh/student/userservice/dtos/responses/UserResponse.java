package fit.iuh.student.userservice.dtos.responses;

import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import jakarta.persistence.*;

import java.time.LocalDate;

public class UserResponse {
    private String userId;

    private String email;

    private String fullname;

    private Gender gender;

    private LocalDate dob;

    private String phone;

    private String address;

    private Role role;

    private Status status;
}
