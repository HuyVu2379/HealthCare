package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpcomingAppointmentDTO {
    private String appointmentId;
    private String time;
    private String patientName;
    private String consultationType;
    private AppointmentStatus status;
}
