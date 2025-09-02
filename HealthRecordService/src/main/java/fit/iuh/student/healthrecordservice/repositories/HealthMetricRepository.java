package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface HealthMetricRepository extends JpaRepository<HealthMetric,String> {
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.patientId = :patientId AND hm.metricName = 'eGFR' ORDER BY hm.measuredAt DESC")
    HealthMetric getEGFRMetric(String patientId);
}
