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
public class PrescriptionGroupResponse {
    private String medicalRecordId;

    private String doctorId;

    private String doctorName;

    private Date createdDate;

    private Date appointmentDate;

    private String diagnosis;

    private String serviceName;

    private boolean isActive; // Còn hiệu lực (có ít nhất 1 thuốc chưa hết hạn)

    private List<PrescriptionResponse> prescriptions;

    private int totalMedicines; // Tổng số thuốc trong toa
}
