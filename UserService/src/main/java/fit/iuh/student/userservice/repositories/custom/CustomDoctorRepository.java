package fit.iuh.student.userservice.repositories.custom;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;

import java.util.List;

public interface CustomDoctorRepository {
    UpdateDoctorResponse updateDoctor(UpdateDoctorRequest request);
    UpdateDoctorCertificationResponse updateDoctorCertification(UpdateDoctorCertificationRequest request, String userId);
}
