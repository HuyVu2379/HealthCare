package fit.iuh.student.schedulingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BulkCreateDoctorScheduleResponse {
    private List<DoctorScheduleResponse> createdSchedules;
}
