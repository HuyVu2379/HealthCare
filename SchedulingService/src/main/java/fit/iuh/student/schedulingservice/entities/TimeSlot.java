package fit.iuh.student.schedulingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Table(name = "time_slots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlot extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "slot_id")
    private Integer slotId;

    private Timestamp startTime;
    private Timestamp endTime;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "schedule_id")
//    private DoctorSchedule doctorSchedule;
//
//    @OneToOne(mappedBy = "timeSlot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private Appointment appointment;
}
