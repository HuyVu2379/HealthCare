package fit.iuh.student.healthrecordservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private int recordId;
    
    @Column(name = "appointment_id")
    private int appointmentId;
    
    @Column(length = 1000)
    private String diagnosis;
    
    @Column(length = 1000)
    private String treatment;
    
    @Column(length = 1000)
    private String symptoms;
    
    @Column(name = "follow_up_date")
    private Date followUpDate;

    @Column(name = "image_path")
    private List<String> imageAttachments = new ArrayList<>();
    
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthMetric> healthMetrics = new ArrayList<>();
    
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();
    
    @Column(name = "doctor_note", length = 2000)
    private String doctorNote;
    
    // Helper methods to maintain bidirectional relationships
    public void addHealthMetric(HealthMetric healthMetric) {
        healthMetrics.add(healthMetric);
        healthMetric.setMedicalRecord(this);
    }
    
    public void removeHealthMetric(HealthMetric healthMetric) {
        healthMetrics.remove(healthMetric);
        healthMetric.setMedicalRecord(null);
    }
    
    public void addPrescription(Prescription prescription) {
        prescriptions.add(prescription);
        prescription.setMedicalRecord(this);
    }
    
    public void removePrescription(Prescription prescription) {
        prescriptions.remove(prescription);
        prescription.setMedicalRecord(null);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord)) return false;
        MedicalRecord that = (MedicalRecord) o;
        return recordId == that.recordId;
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
