package fit.iuh.student.userservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import fit.iuh.student.userservice.dtos.CertificationDto;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDoctorCertificationResponse {
    List<CertificationDto> certifications;
}
