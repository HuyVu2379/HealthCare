package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric,String> {
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.patientId = :patientId AND hm.metricName = 'eGFR' ORDER BY hm.measuredAt DESC")
    HealthMetric getEGFRMetric(String patientId);

    @Query(value = "SELECT * FROM health_metric hm WHERE hm.patient_id = :patientId AND hm.measured_at >= DATE_SUB(CURDATE(), INTERVAL :month MONTH) ORDER BY hm.measured_at DESC", nativeQuery = true)
    List<HealthMetric> findHealthMetricFilterAll(String patientId,int month);

    @Query(value = "SELECT * FROM health_metric hm WHERE hm.patient_id = :patientId AND hm.metric_name = :metricName AND hm.measured_at >= DATE_SUB(CURDATE(), INTERVAL :month MONTH) ORDER BY hm.measured_at DESC", nativeQuery = true)
    List<HealthMetric> findHealthMetricFilterByMetricName(String patientId, String metricName, int month);
}
