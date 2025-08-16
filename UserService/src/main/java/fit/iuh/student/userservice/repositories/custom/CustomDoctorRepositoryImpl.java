package fit.iuh.student.userservice.repositories.custom;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Repository
public class CustomDoctorRepositoryImpl implements CustomDoctorRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public UpdateDoctorResponse updateDoctor(UpdateDoctorRequest request) {
        Doctor doctor = entityManager.find(Doctor.class,request.getUserId());
        if(doctor == null){
            throw new UserNotFoundException("Doctor not found");
        }
        doctor.setSpecialty(request.getSpecialty());
        doctor.setExperienceYears(request.getExperienceYears());
        doctor.setBio(request.getBio());

        entityManager.merge(doctor);

        return UpdateDoctorResponse.builder()
                .address(doctor.getAddress())
                .avatarUrl(doctor.getAvatarUrl())
                .bio(doctor.getBio())
                .dob(doctor.getDob())
                .email(doctor.getEmail())
                .fullname(doctor.getFullname())
                .gender(doctor.getGender())
                .phone(doctor.getPhone())
                .specialty(doctor.getSpecialty())
                .role(doctor.getRole())
                .certifications(doctor.getCertifications())
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

        doctor.setCertifications(request.getCertifications());
        entityManager.merge(doctor);

        return UpdateDoctorCertificationResponse.builder()
                .certifications(request.getCertifications())
                .build();
    }
}
