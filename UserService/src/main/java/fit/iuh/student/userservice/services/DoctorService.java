package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.DoctorResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;

import java.util.List;

public interface DoctorService {
    UpdateDoctorResponse updateDoctor(UpdateDoctorRequest updateDoctorRequest);
    UpdateDoctorCertificationResponse updateDoctorCertification(UpdateDoctorCertificationRequest request, String doctorId);
    DoctorResponse getDoctorById(String doctorId);
}
