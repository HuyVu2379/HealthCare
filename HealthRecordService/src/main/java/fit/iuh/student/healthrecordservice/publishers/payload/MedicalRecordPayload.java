package fit.iuh.student.healthrecordservice.publishers.payload;
import fit.iuh.student.healthrecordservice.clients.dtos.PatientClientResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordPayload {
    private String appointmentId;
    private String serviceName; // Tên dịch vụ y tế
    private String diagnosis; // Chẩn đoán
    private String treatment; // Phương pháp điều trị
    private String symptoms; // Triệu chứng
    private Date dateDiagnosis; // Ngày chẩn đoán
    private String doctorNote;
    private String signature;
    private int stage;
    private PatientClientResponse patient;
    private String statusHealth;
    private String eventType;
}
