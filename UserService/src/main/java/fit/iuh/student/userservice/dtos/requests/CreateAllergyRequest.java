package fit.iuh.student.userservice.dtos.requests;
import fit.iuh.student.userservice.enums.AllergyLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAllergyRequest {
    private String name;
    private String description;
    private AllergyLevel level;
    private String patientId;
}
