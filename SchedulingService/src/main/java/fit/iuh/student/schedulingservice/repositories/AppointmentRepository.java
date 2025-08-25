package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
    @Query("Select a from Appointment a where a.patientId = ?1 and a.appointmentDate between ?2 and ?3")
    Page<Appointment> findAppointmentByPatientIdWithPage(String patientId, Date startTime, Date endTime, Pageable pageable);
}
