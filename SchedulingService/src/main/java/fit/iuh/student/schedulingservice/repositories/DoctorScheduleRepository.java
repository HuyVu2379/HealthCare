package fit.iuh.student.schedulingservice.repositories;

import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule,String> {
    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.doctorId = ?1 AND ds.workDate = ?2")
    DoctorSchedule getDoctorScheduleByDate(String doctorId, Date date);
    @Query("SELECT DISTINCT ds.doctorId FROM DoctorSchedule ds WHERE ds.workDate = ?1")
    List<String> findDoctorIdsByDate(Date date);
    
    @Query("SELECT CASE WHEN COUNT(ds) > 0 THEN true ELSE false END FROM DoctorSchedule ds WHERE ds.doctorId = ?1 AND ds.workDate = ?2")
    boolean existsByDoctorIdAndWorkDate(String doctorId, Date workDate);

    @Query("SELECT ds.workDate FROM DoctorSchedule ds WHERE ds.doctorId = :doctorId")
    List<Date> findWorkDatesByDoctorId(@Param("doctorId") String doctorId);

    @Query("SELECT ds FROM DoctorSchedule ds LEFT JOIN FETCH ds.timeSlots WHERE ds.scheduleId = :scheduleId")
    Optional<DoctorSchedule> findWithSlotsById(@Param("scheduleId") String scheduleId);

    @Query("SELECT ds FROM DoctorSchedule ds WHERE ds.workDate = ?1")
    List<DoctorSchedule> findDoctorScheduleByWorkDate(Date workDate);
}
