package fit.iuh.student.schedulingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentClientResponse {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private Date appointmentDate; 
    private String consultationType;
    private String relatedRecordId;
}
