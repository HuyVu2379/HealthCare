package fit.iuh.student.healthrecordservice.entities;

import fit.iuh.student.healthrecordservice.enums.EpisodeType;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "medical_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MedicalRecord extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "record_id")
    private String recordId;

    private String patientId; // Mã bệnh nhân

    private String doctorId; // Mã bác sĩ

    private String serviceName; // Tên dịch vụ y tế

    @Column(name = "appointment_id", nullable = false, unique = true)
    private String appointmentId;
    
    @Column(length = 1000)
    private String diagnosis; // Chẩn đoán
    
    @Column(length = 1000)
    private String treatment; // Phương pháp điều trị
    
    @Column(length = 1000)
    private String symptoms; // Triệu chứng
    
    @Column(name = "follow_up_date")
    private Date followUpDate; // Ngày tái khám

    @Column(name = "image_path")
    private List<String> imageAttachments = new ArrayList<>();

    @Column(name = "signature_url", columnDefinition = "TEXT")
    private String signatureUrl; // URL chữ ký bác sĩ

    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<HealthMetric> healthMetrics = new ArrayList<>();
    
    @OneToMany(mappedBy = "medicalRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Prescription> prescriptions = new ArrayList<>();
    
    @Column(name = "doctor_note", length = 2000)
    private String doctorNote;

    @Column(name = "parent_record_id")
    private String parentRecordId; // Link đến medical record trước đó

    @Enumerated(EnumType.STRING)
    @Column(name = "episode_type", length = 20)
    private EpisodeType episodeType; // INITIAL, FOLLOW_UP

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
        if (!(o instanceof MedicalRecord that)) return false;
        return Objects.equals(appointmentId, that.appointmentId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(appointmentId);
    }
}
