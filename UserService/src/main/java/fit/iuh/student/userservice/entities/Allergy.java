package fit.iuh.student.userservice.entities;

import fit.iuh.student.userservice.enums.AllergyLevel;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Allergy extends BaseEntity{
    @Id
    @Column(name = "allergy_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String allergyId;
    private String name;
    private String description;
    private AllergyLevel level;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id")
    private Patient patient;
}
