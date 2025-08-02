package fit.iuh.student.userservice.entities;

import fit.iuh.student.userservice.enums.Gender;
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
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "user_id")
public class Patient extends User {
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> medicalHistories;
}
