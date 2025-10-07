package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,String> {
    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM MedicalRecord m WHERE m.appointmentId = ?1")
    boolean existsAppointmentId(String appointmentId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.appointmentId = ?1")
    MedicalRecord findByAppointmentId(String appointmentId);

    @Query("SELECT m FROM MedicalRecord m WHERE m.patientId = ?1")
    Page<MedicalRecord> findByPatientId(String patientId, Pageable pageable);
}
