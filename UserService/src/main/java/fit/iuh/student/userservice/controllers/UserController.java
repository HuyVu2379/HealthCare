package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.UpdateUserRequest;
import fit.iuh.student.userservice.dtos.responses.MessageResponse;
import fit.iuh.student.userservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.userservice.dtos.responses.UserResponse;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
