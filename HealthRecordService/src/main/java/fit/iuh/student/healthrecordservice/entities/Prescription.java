package fit.iuh.student.healthrecordservice.entities;

import fit.iuh.student.healthrecordservice.enums.Frequency;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "prescriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Prescription extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "prescription_id")
    private String prescriptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private MedicalRecord medicalRecord;
    
    @Column(name = "medical_name", nullable = false)
    private String medicalName; // tên thuốc
    
    @Column(nullable = false)
    private String dosage; // liều lượng
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private List<Frequency> frequency; // tần suất sử dụng
    
    @Column(length = 1000)
    private String notes; // ghi chú
    
    @Column(nullable = false)
    private String duration; // thời gian sử dụng
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Prescription)) return false;
        Prescription that = (Prescription) o;
        return prescriptionId == that.prescriptionId;
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
