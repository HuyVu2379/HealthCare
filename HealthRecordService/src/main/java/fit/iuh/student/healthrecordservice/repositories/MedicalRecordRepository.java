package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,String> {
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MedicalRecord m WHERE m.appointmentId = ?1")
    boolean existsAppointmentId(String appointmentId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.appointmentId = ?1")
    MedicalRecord findByAppointmentId(String appointmentId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patientId = ?1")
    Page<MedicalRecord> findByPatientId(String patientId, Pageable pageable);

    // ========== NEW QUERIES FOR FOLLOW-UP SYSTEM ==========

    // Find all follow-up records of a parent record (sorted by created date)
    @Query("SELECT m FROM MedicalRecord m WHERE m.parentRecordId = ?1 ORDER BY m.createdAt ASC")
    List<MedicalRecord> findFollowUpRecords(String parentRecordId);

    // Count follow-ups of a record
    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE m.parentRecordId = ?1")
    Long countFollowUpRecords(String parentRecordId);

    // Find INITIAL records only (for episodes list)
    @Query("SELECT m FROM MedicalRecord m WHERE m.patientId = ?1 AND (m.episodeType = 'INITIAL' OR m.episodeType IS NULL)")
    Page<MedicalRecord> findInitialRecordsByPatientId(String patientId, Pageable pageable);

    // ========== NEW QUERIES FOR FULL TIMELINE ==========

    // Lấy TẤT CẢ records của patient với doctor, sorted by created date DESC
    @Query("SELECT m FROM MedicalRecord m WHERE m.patientId = ?1 AND m.doctorId = ?2 ORDER BY m.createdAt DESC")
    List<MedicalRecord> findByPatientIdAndDoctorIdOrderByCreatedAtDesc(String patientId, String doctorId);

    // Count total records of patient with doctor
    @Query("SELECT COUNT(m) FROM MedicalRecord m WHERE m.patientId = ?1 AND m.doctorId = ?2")
    Long countByPatientIdAndDoctorId(String patientId, String doctorId);

    // ========== DASHBOARD QUERIES ==========

    // Find recent medical records by doctor (for dashboard)
    @Query("SELECT m FROM MedicalRecord m WHERE m.doctorId = ?1 ORDER BY m.createdAt DESC")
    Page<MedicalRecord> findRecentByDoctorId(String doctorId, Pageable pageable);
}
