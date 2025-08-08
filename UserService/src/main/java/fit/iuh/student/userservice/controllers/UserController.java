package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.MessageResponse;
import fit.iuh.student.userservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.services.UploadService;
import fit.iuh.student.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

@RestController()
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    @PutMapping("/update")
    public ResponseEntity<MessageResponse<UserResponse>> updateUser(
            @RequestBody UpdateUserRequest updateUserRequest
    ) {
        UserResponse userResponse = userService.updateUser(updateUserRequest);
        if (userResponse == null) {
            throw new UserNotFoundException("User not found");
        }
        return SuccessEntityResponse.ok("Update user successfully", userResponse);
    }
    @PutMapping(value = "/update-avatar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageResponse<String>> updateAvatar(
            @RequestPart FilePart file,
            @PathVariable String id
    ) {
        String imageUrl = userService.updateUserAvatar(id, file);
        if (imageUrl == null || imageUrl.isEmpty()) {
            throw new UserNotFoundException("User not found or image upload failed");
        }
        return SuccessEntityResponse.ok("Update avatar successfully", imageUrl);
    }

}
