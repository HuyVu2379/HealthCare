package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription,String> {
    @Query("SELECT p FROM Prescription p WHERE p.medicalRecord.patientId = ?1 AND p.endDate >= CURRENT_DATE")
    List<Prescription> findPrescriptionUsing(String patientId);

    @Query("SELECT p FROM Prescription p WHERE p.medicalRecord.recordId = ?1")
    List<Prescription> findByMedicalRecordId(String recordId);
}
