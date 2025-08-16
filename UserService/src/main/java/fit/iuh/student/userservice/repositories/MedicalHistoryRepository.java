package fit.iuh.student.userservice.repositories;

import fit.iuh.student.userservice.entities.MedicalHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, Integer> {
    @Query("SELECT mh from MedicalHistory mh where mh.patient.userId = ?1")
    List<MedicalHistory> getMedicalHistoriesByPatientId(String patientId);

    // Pageable tự động xử lý pagination và sorting
    @Query("SELECT mh FROM MedicalHistory mh WHERE mh.patient.userId = ?1")
    Page<MedicalHistory> findByPatientUserId(String patientId, Pageable pageable);
}
