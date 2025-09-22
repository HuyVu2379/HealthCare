package fit.iuh.student.userservice.dtos.responses;

import fit.iuh.student.userservice.enums.AllergyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AllergyResponse {
    private String allergyId;
    private String name;
    private String description;
    private AllergyLevel level;
    private String patientId;
}
