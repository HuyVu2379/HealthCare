package fit.iuh.student.schedulingservice.consumers.payload;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleEventMessage {
    private String appointmentId = null;
    private String patientId = null;
    private String doctorId = null;
    private ScheduleSocketEvent event = null;
    private AppointmentStatus status = null;
    private Boolean hasPredict = null;
    private CreateAppointmentRequest createAppointmentRequest = null;
    private UpdateAppointmentRequest updateAppointmentRequest = null;
}
