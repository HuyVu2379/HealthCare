package fit.iuh.student.userservice.mappers;

import fit.iuh.student.userservice.dtos.responses.DoctorClientResponse;
import fit.iuh.student.userservice.dtos.responses.DoctorResponse;
import fit.iuh.student.userservice.dtos.responses.PatientClientResponse;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.entities.Patient;
import fit.iuh.student.userservice.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toUserResponse(User user);
    DoctorResponse toDoctorResponse(Doctor doctor);
    DoctorClientResponse toDoctorClientResponse(Doctor doctor);
    PatientClientResponse toPatientClientResponse(Patient patient);
}
