package fit.iuh.student.schedulingservice.entities;

import fit.iuh.student.schedulingservice.enums.WeekDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DoctorSchedule extends BaseEntity {
    @Id
    @Column(name = "schedule_id")
    private String scheduleId;
    private String doctorId;
    private WeekDay weekDay;
    private Date workDate;
    private boolean isAvailable;
    
    @OneToMany(mappedBy = "doctorSchedule", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeSlot> timeSlots = new ArrayList<>();
    
    // Helper method to add a time slot
    public void addTimeSlot(TimeSlot timeSlot) {
        timeSlots.add(timeSlot);
    }
    
    // Helper method to remove a time slot
    public void removeTimeSlot(TimeSlot timeSlot) {
        timeSlots.remove(timeSlot);
    }
}
