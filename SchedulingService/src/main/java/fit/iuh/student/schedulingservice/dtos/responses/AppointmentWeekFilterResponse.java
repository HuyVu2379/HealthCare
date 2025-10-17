package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.WeekDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentWeekFilterResponse {
    private Date date;
    private WeekDay dayOfWeek;
    private String appointmentId;
    private AppointmentStatus status;
    private String patientName;
    private String patientId;
    private TimeSlotDTO timeSlot;
    private boolean hasPredict;
    private String note;
    private String symptoms;
}
