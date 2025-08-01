package fit.iuh.student.userservice.entities;

import fit.iuh.student.userservice.enums.Gender;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "password", nullable = false)
    private String password;
    
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "fullname", nullable = false)
    private String fullname;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "dob")
    private LocalDate dob;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address")
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;
}
