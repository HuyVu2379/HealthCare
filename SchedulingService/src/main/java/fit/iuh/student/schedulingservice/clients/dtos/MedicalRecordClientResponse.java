package fit.iuh.student.schedulingservice.clients.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordClientResponse {
    private String recordId;
    private String patientId;
    private String diagnosis;
    private Date createdAt;
}
