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
public class MedicalRecordDetailResponse {
    private String recordId;
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private String doctorName;
    private String serviceName;
    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String doctorNote;
    private Date followUpDate;
    private List<String> imageAttachments;
    private String signatureUrl;
    private Integer stage;
    private String statusHealth;
    private Date createdAt;
    private Date updatedAt;
    private List<PrescriptionResponse> prescriptions;
}
