package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
}
