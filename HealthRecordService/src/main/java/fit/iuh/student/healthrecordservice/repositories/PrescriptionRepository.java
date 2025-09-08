package fit.iuh.student.healthrecordservice.repositories;

import fit.iuh.student.healthrecordservice.entities.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription,String> {
}
