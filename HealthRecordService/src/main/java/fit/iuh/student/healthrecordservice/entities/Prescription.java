package fit.iuh.student.healthrecordservice.entities;

import fit.iuh.student.healthrecordservice.enums.Frequence;
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
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "prescription_id")
    private int prescriptionId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private MedicalRecord medicalRecord;
    
    @Column(name = "medical_name", nullable = false)
    private String medicalName;
    
    @Column(nullable = false)
    private String dosage;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private List<Frequence> frequency;
    
    @Column(length = 1000)
    private String notes;
    
    @Column(nullable = false)
    private String duration;
    
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
