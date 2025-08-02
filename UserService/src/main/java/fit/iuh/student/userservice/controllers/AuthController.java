package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.AuthenticationRequest;
import fit.iuh.student.userservice.dtos.requests.RegisterRequest;
import fit.iuh.student.userservice.dtos.responses.*;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.services.AuthenticationService;
import fit.iuh.student.userservice.services.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        return SuccessEntityResponse.ok("Registration successful", authenticationService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse<LoginResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return SuccessEntityResponse.ok("Login successful", authenticationService.login(request));
    }

    @PostMapping("/refresh-token")
    public void refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {
        authenticationService.refreshToken(request, response);
    }

    @GetMapping("/send-otp-register/{email}")
    public ResponseEntity<MessageResponse<Boolean>> sendOtpRegister(
            @PathVariable String email
    ){
        emailService.sendOTPEmail(email,"Xác minh tài khoản");
        return SuccessEntityResponse.ok("OTP sent successfully", true);
    }
    /*
       Nếu là otp reset password thì email + "-reset-pwd"
     */
    @GetMapping("/validate-otp")
    public ResponseEntity<MessageResponse<Boolean>> validateOtp(
            @QueryParam("email") String email,
            @QueryParam("otp") String otp
    ) {
        boolean isValid = emailService.validateOTP(email,otp);
        if (isValid) {
            return SuccessEntityResponse.ok("OTP is valid", true);
        } else {
            MessageResponse<Boolean> response = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "otp is invalid or has expired",
                    false,
                    isValid
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/send-otp-reset-password/{email}")
    public ResponseEntity<MessageResponse<Object>> sendOtpResetPassword(
            @PathVariable String email
    ) {
        ResetPasswordResponse response = emailService.sendOTPResetPassword(email, "Xác minh reset mật khẩu");
        if (response != null && response.getStatusCode() == HttpStatus.OK.value()) {
            return SuccessEntityResponse.ok("OTP sent successfully", response);
        } else {
            throw new UserNotFoundException("User not found with email: " + email);
        }
    }
}
