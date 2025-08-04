package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.UserResponse;

public interface UserService {
    UserResponse updateUser(UpdateUserRequest updateUserRequest);
    String updateUserAvatar(String userId, String avatarUrl);
}
