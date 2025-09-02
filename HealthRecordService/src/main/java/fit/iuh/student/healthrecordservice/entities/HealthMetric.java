package fit.iuh.student.healthrecordservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Table(name = "health_metrics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HealthMetric extends BaseEntity {
    public HealthMetric(String patientId, String metricName, int metricValue, String unit, MedicalRecord medicalRecord, Date measuredAt) {
        this.patientId = patientId;
        this.metricName = metricName;
        this.metricValue = metricValue;
        this.unit = unit;
        this.medicalRecord = medicalRecord;
        this.measuredAt = measuredAt;
    }
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "metric_id")
    private String metricId;
    
    @Column(name = "patient_id")
    private String patientId;
    
    @Column(name = "metric_type", nullable = false)
    private String metricName;
    
    @Column(name = "metric_value", nullable = false)
    private int metricValue;
    
    @Column(nullable = false)
    private String unit;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id")
    private MedicalRecord medicalRecord;
    
    @Column(name = "measured_at")
    private Date measuredAt;
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof HealthMetric)) return false;
        HealthMetric that = (HealthMetric) o;
        return metricId == that.metricId;
    }
    
    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
