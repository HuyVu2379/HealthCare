package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord,String> {
}
