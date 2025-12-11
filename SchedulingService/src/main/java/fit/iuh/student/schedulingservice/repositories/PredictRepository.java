package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Predict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictRepository extends JpaRepository<Predict, String> {

    // Lấy record thứ 2 mới nhất theo patientId (để so sánh trend với prediction hiện tại)
    @Query(value = "SELECT * FROM predict WHERE patient_id = :patientId ORDER BY created_at DESC LIMIT 1 OFFSET 1", nativeQuery = true)
    Predict findLatestPredictByPatientId(@Param("patientId") String patientId);

    @Query("SELECT p FROM Predict p WHERE p.patientId = :patientId ORDER BY p.createdAt DESC LIMIT 2")
    List<Predict> findPredictByPatientId(String patientId);
}
