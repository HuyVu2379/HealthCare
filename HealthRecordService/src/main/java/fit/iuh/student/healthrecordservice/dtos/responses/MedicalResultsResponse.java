package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalResultsResponse {
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalRecordItem {
        private String recordId;
        private String appointmentId;
        private String diagnosis;
        private String symptoms;
        private String treatment;
        private String doctorNote;
        private Date followUpDate;
        private String serviceName;
        private String statusHealth;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    private MedicalRecordItem medicalRecord;
    private List<PrescriptionItem> prescriptions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PrescriptionItem {
        private String prescriptionId;
        private String medicalRecordId;
        private String medicalName;
        private String dosage;
        private List<fit.iuh.student.healthrecordservice.enums.Frequency> frequency;
        private Date startDate;
        private Date endDate;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}


