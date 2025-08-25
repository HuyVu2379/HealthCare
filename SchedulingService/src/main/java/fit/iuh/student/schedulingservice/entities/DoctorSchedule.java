package fit.iuh.student.schedulingservice.entities;

import fit.iuh.student.schedulingservice.enums.WeekDay;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctor_schedules",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "unique_doctor_workdate",
                        columnNames = {"doctor_id", "work_date"} // Unique trên cặp này
                )
        }
)
public class DoctorSchedule extends BaseEntity {
    @Id
    @Column(name = "schedule_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private String scheduleId;
    private String doctorId;
    private WeekDay weekDay;
    private Date workDate;
    private boolean isAvailable;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "doctor_schedule_time_slots",
            joinColumns = @JoinColumn(name = "schedule_id"),
            inverseJoinColumns = @JoinColumn(name = "slot_id"),
            uniqueConstraints = @UniqueConstraint(
                    name = "uk_schedule_slot",
                    columnNames = {"schedule_id", "slot_id"}
            )
    )
    private List<TimeSlot> timeSlots = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<Appointment> appointments;
    // Helper methods
    public void addTimeSlot(TimeSlot timeSlot) {
        timeSlots.add(timeSlot);
    }

    public void removeTimeSlot(TimeSlot timeSlot) {
        timeSlots.remove(timeSlot);
    }
}
