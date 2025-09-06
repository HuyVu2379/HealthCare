package fit.iuh.student.userservice.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import fit.iuh.student.userservice.enums.StatusHealth;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "medical_histories")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalHistory extends BaseEntity {
    public MedicalHistory(Patient patient,Doctor doctor, String serviceName, LocalDate diagnosisDate, String notes, int stage, String diagnosis, StatusHealth statusHealth) {
        this.patient = patient;
        this.doctor = doctor;
        this.serviceName = serviceName;
        this.diagnosisDate = diagnosisDate;
        this.notes = notes;
        this.stage = stage;
        this.diagnosis = diagnosis;
        this.statusHealth = statusHealth;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "medical_history_id")
    private Integer medicalHistoryId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private Patient patient;

    @ManyToOne(fetch = FetchType.LAZY)
    private Doctor doctor;

    @Column(name = "service_name", nullable = false)
    private String serviceName;

    @Column(name = "diagnosis_date", nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy")
    private LocalDate diagnosisDate;

    @Column(name = "diagnosis", nullable = false)
    private String diagnosis;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name="stage")
    private int stage;

    @Column(name = "status_health", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusHealth statusHealth;
}
