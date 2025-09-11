package fit.iuh.student.healthrecordservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordRequest {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String serviceName;
    private String diagnosis;
    private String treatment;
    private String symptoms;
    private String followUpDate;
    private String doctorNote;
    private List<String> imageAttachments;
    private int stage;
    private String statusHealth;
}
