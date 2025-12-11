package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Predict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredictRepository extends JpaRepository<Predict, String> {

    // Lấy record thứ 2 mới nhất theo patientId (sắp xếp theo createdAt giảm dần, bỏ qua phần tử đầu tiên)
    @Query("SELECT p FROM Predict p WHERE p.patientId = :patientId ORDER BY p.createdAt DESC LIMIT 1 OFFSET 1")
    Predict findLatestPredictByPatientId(@Param("patientId") String patientId);

    @Query("SELECT p FROM Predict p WHERE p.patientId = :patientId ORDER BY p.createdAt DESC LIMIT 2")
    List<Predict> findPredictByPatientId(String patientId);
}
