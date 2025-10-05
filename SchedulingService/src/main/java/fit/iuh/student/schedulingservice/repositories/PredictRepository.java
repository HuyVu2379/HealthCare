package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Predict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PredictRepository extends JpaRepository<Predict, String> {

    // Lấy record mới nhất theo patientId (sắp xếp theo predictId giảm dần)
    @Query("SELECT p FROM Predict p WHERE p.patientId = :patientId ORDER BY p.predictId DESC LIMIT 1")
    Predict findLatestPredictByPatientId(@Param("patientId") String patientId);
}
