package fit.iuh.student.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "doctors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Doctor extends User{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doctor_id")
    private Integer doctorId;
    
    @Column(name = "specialty")
    private String specialty;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "certifications")
    private List<String> certifications;
}
