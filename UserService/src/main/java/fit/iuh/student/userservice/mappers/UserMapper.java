package fit.iuh.student.userservice.mappers;

import fit.iuh.student.userservice.dtos.CertificationDto;
import fit.iuh.student.userservice.dtos.responses.DoctorClientResponse;
import fit.iuh.student.userservice.dtos.responses.DoctorResponse;
import fit.iuh.student.userservice.dtos.responses.PatientClientResponse;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.entities.Certification;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.entities.Patient;
import fit.iuh.student.userservice.entities.User;
import org.mapstruct.Mapper;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    DoctorResponse toDoctorResponse(Doctor doctor);
    DoctorClientResponse toDoctorClientResponse(Doctor doctor);
    PatientClientResponse toPatientClientResponse(Patient patient);
    
    // Certification mapping
    CertificationDto toCertificationDto(Certification certification);
    
    // Custom mapping method for converting List<Certification> to List<CertificationDto>
    default List<CertificationDto> map(List<Certification> certifications) {
        if (certifications == null) {
            return null;
        }
        return certifications.stream()
                .map(this::toCertificationDto)
                .collect(Collectors.toList());
    }
}
