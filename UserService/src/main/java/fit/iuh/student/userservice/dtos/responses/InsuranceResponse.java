package fit.iuh.student.userservice.dtos.responses;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceResponse {
    private String insuranceId;
    private String insuranceName;
    private Date insuranceEndDate;
    private String patientId;
}
