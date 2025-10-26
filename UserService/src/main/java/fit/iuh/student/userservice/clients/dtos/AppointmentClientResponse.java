package fit.iuh.student.userservice.clients.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentClientResponse {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String consultationType;
    private String relatedRecordId;
}
