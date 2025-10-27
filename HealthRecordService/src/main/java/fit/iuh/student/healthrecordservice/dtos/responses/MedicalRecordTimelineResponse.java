package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordTimelineResponse {
    private MedicalRecordDetailResponse rootRecord;           // Lần khám đầu tiên
    private List<MedicalRecordDetailResponse> followUpRecords; // Các lần tái khám
}
