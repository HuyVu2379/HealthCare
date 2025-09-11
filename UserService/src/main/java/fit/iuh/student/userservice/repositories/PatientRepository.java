package fit.iuh.student.userservice.repositories;

import fit.iuh.student.userservice.entities.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient,String> {
    @Query("SELECT DISTINCT p FROM Patient p " +
            "JOIN p.medicalHistories mh " +
            "JOIN mh.doctor d " +
            "WHERE d.userId = :doctorId " +
            "AND (:namePatient IS NULL OR lower(p.fullName) LIKE lower(CONCAT('%', :namePatient, '%')) ) " +
            "AND (:statusHealth IS NULL OR mh.statusHealth = :statusHealth)")
    Page<Patient> findPatientsByDoctorId(String doctorId, Pageable pageable, String namePatient, String statusHealth);
}
