package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.mappers.UserMapper;
import fit.iuh.student.userservice.repositories.UserRepository;
import fit.iuh.student.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserResponse updateUser(UpdateUserRequest updateUserRequest) {
        try{
            User user = userRepository.findById(updateUserRequest.getUserId()).orElse(null);
            if(user != null){
                user.setFullname(updateUserRequest.getFullname());
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
    public String updateUserAvatar(String userId, String avatarUrl) {
        return "";
    }
}
