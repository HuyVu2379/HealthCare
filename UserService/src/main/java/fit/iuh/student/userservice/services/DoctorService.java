package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.CertificationDto;
import fit.iuh.student.userservice.dtos.requests.AddCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.CreateDoctorAccountRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.DoctorClientResponse;
import fit.iuh.student.userservice.dtos.responses.DoctorResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;

import java.util.List;

public interface DoctorService {
    UpdateDoctorResponse updateDoctor(UpdateDoctorRequest updateDoctorRequest);
    UpdateDoctorCertificationResponse updateDoctorCertification(UpdateDoctorCertificationRequest request, String doctorId);
    DoctorResponse getDoctorById(String doctorId);
    List<DoctorResponse> getDoctorByIds(List<String> doctorIds);
    DoctorClientResponse getDoctorIdByIdForClient(String doctorId);
    DoctorResponse createAccountForDoctor(CreateDoctorAccountRequest request);
    
    // New certification methods
    CertificationDto addCertification(AddCertificationRequest request, String userId);
    CertificationDto updateCertification(UpdateCertificationRequest request, String userId, String certificationId);
    void deleteCertification(String userId, String certificationId);
    List<CertificationDto> getCertificationsByUserId(String userId);
}



