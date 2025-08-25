package fit.iuh.student.schedulingservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {
    private String appointmentId;
    private String oldScheduleId;
    private String newScheduleId;
    private Integer oldSlotId;
    private Date newAppointmentDate;
    private Integer newSlotId;
}
