package fit.iuh.student.healthrecordservice.dtos.responses;

import fit.iuh.student.healthrecordservice.clients.dtos.PatientClientResponse;
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
    private PatientClientResponse patient;  // Thông tin bệnh nhân (fullName, email, phone, avatarUrl)
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
    private Date appointmentDate;         // Appointment date from SchedulingService
    private List<PrescriptionResponse> prescriptions;

    // ========== NEW FIELDS FOR FOLLOW-UP SYSTEM ==========
    private String parentRecordId;        // Link to parent medical record
    private String episodeType;           // INITIAL or FOLLOW_UP
}
