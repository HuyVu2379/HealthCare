package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,String> {
    @Query("Select a from Appointment a where a.patientId = ?1 and a.appointmentDate between ?2 and ?3")
    Page<Appointment> findAppointmentByPatientIdWithPage(String patientId, Date startTime, Date endTime, Pageable pageable);
    
    @Query(value = "SELECT a.* FROM appointments a " +
           "JOIN time_slots ts ON a.slot_id = ts.slot_id " +
           "WHERE (CAST(a.appointment_date AS timestamp) + ts.start_time::time) BETWEEN CURRENT_TIMESTAMP AND (CURRENT_TIMESTAMP + INTERVAL '12 hours') " +
           "AND a.status = 'CONFIRMED'",
           nativeQuery = true)
    List<Appointment> findAppointmentsForReminder();

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.timeSlot " +
            "WHERE (:type IS NULL OR a.consultationType = :type) " +
            "AND (:status IS NULL OR a.status = :status)")
    Page<Appointment> findAppointmentFilterWithPagination(@Param("type") String type, @Param("status") AppointmentStatus status, Pageable pageable);

    @Query("SELECT a from Appointment a where (:type = 'ALL' OR a.consultationType = :type) and a.patientId = :patientId and (COALESCE(:startTime, a.appointmentDate) = a.appointmentDate OR a.appointmentDate >= :startTime) and (COALESCE(:endTime, a.appointmentDate) = a.appointmentDate OR a.appointmentDate <= :endTime)")
    Page<Appointment> findAppointmentFilterWithPaginationForPatient(@Param("type") ConsultationType type, @Param("patientId") String patientId, @Param("startTime") Date startTime, @Param("endTime") Date endTime, Pageable pageable);

    @Query("SELECT a FROM Appointment a " +
            "LEFT JOIN FETCH a.timeSlot " +
            "WHERE a.doctorId = :doctorId AND (a.appointmentDate BETWEEN :weekStartDate AND :weekEndDate)")
    List<Appointment> findAppointmentsInWeek(@Param("doctorId") String doctorId,@Param("weekStartDate") Date weekStartDate, @Param("weekEndDate") Date weekEndDate);

    @Query("UPDATE Appointment a SET a.status = :status WHERE a.appointmentId = :appointmentId")
    @Modifying
    @Transactional
    void updateAppointmentStatusById(String appointmentId, AppointmentStatus status);

    @Query("SELECT a FROM Appointment a " +
           "WHERE a.doctorSchedule.scheduleId = :scheduleId " +
           "AND a.slotId = :slotId " +
           "AND a.status IN :statuses")
    List<Appointment> findByDoctorScheduleScheduleIdAndSlotIdAndStatusIn(
            @Param("scheduleId") String scheduleId,
            @Param("slotId") Integer slotId,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    // ========== DASHBOARD QUERIES ==========

    // Count appointments for today by doctor and statuses
    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date " +
           "AND a.status IN :statuses")
    Long countTodayAppointmentsByDoctor(
            @Param("doctorId") String doctorId,
            @Param("date") Date date,
            @Param("statuses") List<AppointmentStatus> statuses
    );

    // Count new patients today (first appointment with this doctor today)
    @Query("SELECT COUNT(DISTINCT a.patientId) FROM Appointment a " +
           "WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date " +
           "AND NOT EXISTS (" +
           "  SELECT 1 FROM Appointment a2 " +
           "  WHERE a2.patientId = a.patientId " +
           "  AND a2.doctorId = :doctorId " +
           "  AND a2.appointmentDate < :date" +
           ")")
    Long countNewPatientsToday(
            @Param("doctorId") String doctorId,
            @Param("date") Date date
    );

    // Count completed consultations today
    @Query("SELECT COUNT(a) FROM Appointment a " +
           "WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date " +
           "AND a.status = 'COMPLETED'")
    Long countCompletedToday(
            @Param("doctorId") String doctorId,
            @Param("date") Date date
    );

    // Count total distinct patients of a doctor
    @Query("SELECT COUNT(DISTINCT a.patientId) FROM Appointment a " +
           "WHERE a.doctorId = :doctorId")
    Long countTotalPatientsByDoctor(@Param("doctorId") String doctorId);

    // Find upcoming appointments today (CONFIRMED or PENDING)
    @Query("SELECT a FROM Appointment a " +
           "LEFT JOIN FETCH a.timeSlot " +
           "WHERE a.doctorId = :doctorId " +
           "AND a.appointmentDate = :date " +
           "AND a.status IN ('CONFIRMED', 'PENDING') " +
           "ORDER BY a.timeSlot.startTime ASC")
    List<Appointment> findUpcomingTodayAppointments(
            @Param("doctorId") String doctorId,
            @Param("date") Date date
    );
}
