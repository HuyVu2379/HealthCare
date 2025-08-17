package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.enums.WeekDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateDoctorScheduleResponse {
    private String schedule_id;
    private String doctorId;
    private WeekDay weekDay;
    private Date workDate;
    private boolean isAvailable;
    private List<TimeSlot> timeSlots = new ArrayList<>();
}
