package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.entities.Patient;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.mappers.UserMapper;
import fit.iuh.student.userservice.repositories.PatientRepository;
import fit.iuh.student.userservice.repositories.UserRepository;
import fit.iuh.student.userservice.services.UploadService;
import fit.iuh.student.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UploadService uploadService;
    private final PatientRepository patientRepository;
    @Override
    public UserResponse updateUser(UpdateUserRequest updateUserRequest) {
        try{
            User user = userRepository.findById(updateUserRequest.getUserId()).orElse(null);
            if(user == null){
                throw new UserNotFoundException("User not found with ID: " + updateUserRequest.getUserId());
            }

            if (updateUserRequest.getFullName() != null) {
                user.setFullName(updateUserRequest.getFullName());
            }
            if (updateUserRequest.getGender() != null) {
                user.setGender(updateUserRequest.getGender());
            }
            if (updateUserRequest.getDob() != null) {
                user.setDob(updateUserRequest.getDob());
            }
            if (updateUserRequest.getPhone() != null) {
                user.setPhone(updateUserRequest.getPhone());
            }
            if (updateUserRequest.getAddress() != null) {
                user.setAddress(updateUserRequest.getAddress());
            }
            if (updateUserRequest.getRole() != null) {
                user.setRole(updateUserRequest.getRole());
            }

            user = userRepository.save(user);

            if (user.getRole() == Role.PATIENT && 
                (updateUserRequest.getHeight() != null || 
                 updateUserRequest.getWeight() != null || 
                 updateUserRequest.getBloodType() != null ||
                 updateUserRequest.getBmi() != null)) {
                Patient patient = patientRepository.findById(user.getUserId()).orElse(null);
                if (patient != null) {
                    boolean heightOrWeightUpdated = false;
                    
                    if (updateUserRequest.getHeight() != null) {
                        patient.setHeight(updateUserRequest.getHeight());
                        heightOrWeightUpdated = true;
                    }
                    if (updateUserRequest.getWeight() != null) {
                        patient.setWeight(updateUserRequest.getWeight());
                        heightOrWeightUpdated = true;
                    }
                    if (updateUserRequest.getBloodType() != null) {
                        patient.setBloodType(updateUserRequest.getBloodType());
                    }
                    
                    if (updateUserRequest.getBmi() != null) {
                        patient.setBmi(updateUserRequest.getBmi());
                    } else if (heightOrWeightUpdated) {
                        patient.setBmi(patient.calculateBMI());
                    }
                    
                    patientRepository.save(patient);
                }
            }

            return userMapper.toUserResponse(user);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public String updateUserAvatar(String userId, MultipartFile file) {
        try{
            User user = userRepository.findById(userId).orElse(null);
            if(user == null){
                throw new UserNotFoundException("User not found with ID: " + userId);
            }
            UploadFile uploadfile = uploadService.uploadFile(file,"HealthCare");
            user.setAvatarUrl(uploadfile.getImageUrls().get(0));
            userRepository.save(user);
            return uploadfile.getImageUrls().get(0);
        }catch (Exception e){
            throw e;
        }
    }
}
