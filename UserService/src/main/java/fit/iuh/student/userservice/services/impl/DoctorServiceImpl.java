package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.DoctorResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;
import fit.iuh.student.userservice.mappers.UserMapper;
import fit.iuh.student.userservice.repositories.DoctorRepository;
import fit.iuh.student.userservice.repositories.custom.CustomDoctorRepository;
import fit.iuh.student.userservice.services.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class DoctorServiceImpl implements DoctorService {
    private final CustomDoctorRepository customDoctorRepository;
    private final UserMapper userMapper;
    private final DoctorRepository doctorRepository;

    @Override
    public UpdateDoctorResponse updateDoctor(UpdateDoctorRequest updateDoctorRequest) {
        try{
            return customDoctorRepository.updateDoctor(updateDoctorRequest);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public UpdateDoctorCertificationResponse updateDoctorCertification(UpdateDoctorCertificationRequest request, String doctorId) {
        try{
            return customDoctorRepository.updateDoctorCertification(request, doctorId);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public DoctorResponse getDoctorById(String doctorId) {
        return userMapper.toDoctorResponse(doctorRepository.findById(doctorId).get());
    }
}
