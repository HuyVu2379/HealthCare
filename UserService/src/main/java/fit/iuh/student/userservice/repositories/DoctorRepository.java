package fit.iuh.student.userservice.repositories;

import fit.iuh.student.userservice.entities.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, String> {

    @Modifying
    @Query("UPDATE Doctor d SET d.rating = ?2 WHERE d.userId = ?1")
    int updateRatingForDoctorId(String doctorId, double newRating);
}