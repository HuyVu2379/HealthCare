package fit.iuh.student.schedulingservice.dtos.requests;

import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.enums.WeekDay;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreateDoctorScheduleRequest {
    private String doctorId;
    private WeekDay weekDay;
    private Date workDate;
    private boolean isAvailable;
    private List<Integer> timeSlotIds = new ArrayList<>();
}
