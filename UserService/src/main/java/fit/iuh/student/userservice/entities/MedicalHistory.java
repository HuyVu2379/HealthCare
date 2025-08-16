package fit.iuh.student.userservice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    public MedicalHistory(Patient patient, String condition, LocalDate diagnosisDate, String notes){
        this.patient = patient;
        this.condition = condition;
        this.diagnosisDate = diagnosisDate;
        this.notes = notes;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_history_id")
    private Integer medicalHistoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @Column(name = "condition", nullable = false)
    private String condition;
    
    @Column(name = "diagnosis_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate diagnosisDate;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
