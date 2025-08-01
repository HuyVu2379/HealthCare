package fit.iuh.student.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "medical_histories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_history_id")
    private Integer medicalHistoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @Column(name = "condition", nullable = false)
    private String condition;
    
    @Column(name = "diagnosis_date", nullable = false)
    private LocalDate diagnosisDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
