package fit.iuh.student.userservice.repositories.custom;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.mappers.UserMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Repository
public class CustomDoctorRepositoryImpl implements CustomDoctorRepository {

    @PersistenceContext
    private EntityManager entityManager;
    
    @Autowired
    private UserMapper userMapper;

    @Override
    @Transactional
    public UpdateDoctorResponse updateDoctor(UpdateDoctorRequest request) {
        Doctor doctor = entityManager.find(Doctor.class,request.getUserId());
        if(doctor == null){
            throw new UserNotFoundException("Doctor not found");
        }
        
        if (request.getSpecialty() != null) {
            doctor.setSpecialty(request.getSpecialty());
        }
        if (request.getExperienceYears() != null) {
            doctor.setExperienceYears(request.getExperienceYears());
        }
        if (request.getBio() != null) {
            doctor.setBio(request.getBio());
        }
        if (request.getExaminationFee() != null) {
            doctor.setExaminationFee(request.getExaminationFee());
        }
        if (request.getClinicAddress() != null) {
            doctor.setClinicAddress(request.getClinicAddress());
        }

        entityManager.merge(doctor);

        return UpdateDoctorResponse.builder()
                .address(doctor.getAddress())
                .avatarUrl(doctor.getAvatarUrl())
                .bio(doctor.getBio())
                .dob(doctor.getDob())
                .email(doctor.getEmail())
                .fullName(doctor.getFullName())
                .examinationFee(doctor.getExaminationFee())
                .clinicAddress(doctor.getClinicAddress())
                .gender(doctor.getGender())
                .phone(doctor.getPhone())
                .specialty(doctor.getSpecialty())
                .role(doctor.getRole())
                .certifications(userMapper.map(doctor.getCertifications()))
                .experienceYears(doctor.getExperienceYears())
                .userId(doctor.getUserId())
                .build();
    }

    @Override
    @Transactional
    public UpdateDoctorCertificationResponse updateDoctorCertification(UpdateDoctorCertificationRequest request, String userId) {
        Doctor doctor = entityManager.find(Doctor.class, userId);
        if (doctor == null) {
            throw new UserNotFoundException("Doctor not found");
        }

        // Map List<CertificationDto> from request to current certifications
        entityManager.merge(doctor);

        return UpdateDoctorCertificationResponse.builder()
                .certifications(userMapper.map(doctor.getCertifications()))
                .build();
    }
}
