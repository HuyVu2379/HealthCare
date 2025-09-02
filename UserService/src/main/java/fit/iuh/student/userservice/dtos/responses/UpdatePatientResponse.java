package fit.iuh.student.userservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePatientResponse {
    private String userId;
    private Integer height;
    private double weight;
    private String bloodType;
    private double bmi;
}
