package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
    @Query("Select a from Appointment a where a.patientId = ?1 and a.appointmentDate between ?2 and ?3")
    Page<Appointment> findAppointmentByPatientIdWithPage(String patientId, Date startTime, Date endTime, Pageable pageable);
    
    /**
     * Find appointments that need reminders (12 hours before appointment time)
     * This query finds appointments that:
     * 1. Are scheduled within the next 12 hours
     * 2. Have status CONFIRMED
     */
    @Query(value = "SELECT a.* FROM appointments a " +
           "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
           "WHERE (CAST(a.appointment_date AS timestamp) + ts.start_time::time) BETWEEN CURRENT_TIMESTAMP AND (CURRENT_TIMESTAMP + INTERVAL '12 hours') " +
           "AND a.status = 'CONFIRMED'",
           nativeQuery = true)
    List<Appointment> findAppointmentsForReminder();

    @Query("SELECT a FROM Appointment a " +
            "WHERE (:type IS NULL OR a.consultationType = :type) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Appointment> findAppointmentFilterWithPagination(@Param("type") String type, @Param("status") String status, Pageable pageable);
}
