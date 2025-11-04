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
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    @Mapping(target = "certifications", source = "certifications", qualifiedByName = "mapCertifications")
    DoctorResponse toDoctorResponse(Doctor doctor);
    @Mappings({
            @Mapping(source = "userId", target = "doctorId"),
            @Mapping(source = "phone", target = "phoneNumber"),
            @Mapping(source = "rating", target = "rating"),
            @Mapping(source = "examinationFee", target = "examinationFee")
    })
    DoctorClientResponse toDoctorClientResponse(Doctor doctor);
    PatientClientResponse toPatientClientResponse(Patient patient);
    
    // Certification mapping
    CertificationDto toCertificationDto(Certification certification);
    
    // Custom mapping method for converting List<Certification> to List<CertificationDto>
    @Named("mapCertifications")
    default List<CertificationDto> map(List<Certification> certifications) {
        if (certifications == null) {
            return null;
        }
        return certifications.stream()
                .map(this::toCertificationDto)
                .collect(Collectors.toList());
    }
}
