package fit.iuh.student.schedulingservice.publishers.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppointmentData {
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private String eventType;
    private boolean success;
}
