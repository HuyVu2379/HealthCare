package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.mappers.UserMapper;
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
    @Override
    public UserResponse updateUser(UpdateUserRequest updateUserRequest) {
        try{
            User user = userRepository.findById(updateUserRequest.getUserId()).orElse(null);
            if(user != null){
                user.setFullname(updateUserRequest.getFullName());
                user.setGender(updateUserRequest.getGender());
                user.setDob(updateUserRequest.getDob());
                user.setPhone(updateUserRequest.getPhone());
                user.setAddress(updateUserRequest.getAddress());
                user.setRole(updateUserRequest.getRole());
            }else{
                throw new UserNotFoundException("User not found with ID: " + updateUserRequest.getUserId());
            }
            user = userRepository.save(user);
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
