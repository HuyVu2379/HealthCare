package fit.iuh.student.userservice.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificationDto {
    private String id;
    private String name;
    private String issuingOrganization;
    private Integer yearIssued;
}
