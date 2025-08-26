package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
    @Query("Select a from Appointment a where a.patientId = ?1 and a.appointmentDate between ?2 and ?3")
    Page<Appointment> findAppointmentByPatientIdWithPage(String patientId, Date startTime, Date endTime, Pageable pageable);
    
    /**
     * Find appointments that need reminders (12 hours before appointment time)
     * This query finds appointments that:
     * 1. Are scheduled for tomorrow
     * 2. Have status CONFIRMED
     */
    @Query("SELECT a FROM Appointment a WHERE a.appointmentDate = CURRENT_DATE + 1 AND a.status = 'CONFIRMED'")
    List<Appointment> findAppointmentsForReminder();
}
