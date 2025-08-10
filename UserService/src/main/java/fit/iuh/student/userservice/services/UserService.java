package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;

public interface UserService {
    UserResponse updateUser(UpdateUserRequest updateUserRequest);
    String updateUserAvatar(String userId, MultipartFile file);
}
