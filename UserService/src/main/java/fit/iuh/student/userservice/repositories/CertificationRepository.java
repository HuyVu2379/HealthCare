package fit.iuh.student.userservice.repositories;

import fit.iuh.student.userservice.entities.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, String> {
    
    @Query("SELECT c FROM Certification c WHERE c.doctor.userId = :userId")
    List<Certification> findByDoctorUserId(@Param("userId") String userId);
    
    @Query("SELECT c FROM Certification c WHERE c.id = :certificationId AND c.doctor.userId = :userId")
    Optional<Certification> findByIdAndDoctorUserId(@Param("certificationId") String certificationId, @Param("userId") String userId);
    
    @Query("DELETE FROM Certification c WHERE c.id = :certificationId AND c.doctor.userId = :userId")
    void deleteByIdAndDoctorUserId(@Param("certificationId") String certificationId, @Param("userId") String userId);
}
