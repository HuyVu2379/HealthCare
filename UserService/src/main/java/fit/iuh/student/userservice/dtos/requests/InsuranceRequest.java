package fit.iuh.student.userservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceRequest {
    private String insuranceId;
    private String insuranceName;
    private Date insuranceEndDate;
    private String patientId;
}
