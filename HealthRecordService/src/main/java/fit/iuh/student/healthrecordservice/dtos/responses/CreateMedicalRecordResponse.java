package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordResponse {
    private String recordId;
    private String patientId;
    private String doctorId;
    private String serviceName;
    private String appointmentId;
    private String diagnosis;
    private String treatment;
    private String symptoms;
    private Date followUpDate;
    private List<String> imageAttachments;
    private String doctorNote;
}
