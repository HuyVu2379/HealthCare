package fit.iuh.student.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctors")
@PrimaryKeyJoinColumn(name = "user_id")
public class Doctor extends User{
    @Column(name = "specialty")
    private String specialty;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "certifications")
    private List<String> certifications;
}
