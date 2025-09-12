package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.AuthenticationRequest;
import fit.iuh.student.userservice.dtos.requests.CreateDoctorAccountRequest;
import fit.iuh.student.userservice.dtos.requests.RegisterRequest;
import fit.iuh.student.userservice.dtos.requests.ResetPasswordRequest;
import fit.iuh.student.userservice.dtos.responses.*;
import fit.iuh.student.userservice.exceptions.errors.UnauthorizedException;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.publishers.payload.UserEventPayload;
import fit.iuh.student.userservice.services.AuthenticationService;
import fit.iuh.student.userservice.services.DoctorService;
import fit.iuh.student.userservice.services.EmailService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthenticationService authenticationService;
    private final DoctorService doctorService;
    private final EmailService emailService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse<AuthenticationResponse>> register(
            @RequestBody RegisterRequest request
    ) {
        return SuccessEntityResponse.ok("Registration successful", authenticationService.register(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-doctor")
    public ResponseEntity<MessageResponse<AuthenticationResponse>> registerDoctor(
            @RequestBody RegisterRequest request
    ) {
        return SuccessEntityResponse.ok("Registration doctor successful", authenticationService.registerDoctor(request));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<MessageResponse<AuthenticationResponse>> registerAdmin(
            @RequestBody RegisterRequest request
    ) {
        return SuccessEntityResponse.ok("Registration doctor successful", authenticationService.registerAdmin(request));
    }

    @PostMapping("/login")
    public ResponseEntity<MessageResponse<LoginResponse>> authenticate(
            @RequestBody AuthenticationRequest request
    ) {
        return SuccessEntityResponse.ok("Login successful", authenticationService.login(request));
    }

    @GetMapping("/getMe")
    public ResponseEntity<MessageResponse<Object>> getMe(HttpServletRequest request) {
        try {
            Object result = authenticationService.getMe(request, Object.class);
            if (result == null) {
                throw new UnauthorizedException("Unauthorized access. Please log in again.");
            }
            return SuccessEntityResponse.ok("User details retrieved successfully", result);
        } catch (Exception e) {
            throw e;
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<MessageResponse<AuthenticationResponse>> refreshToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws Exception {
        try {
            AuthenticationResponse authenticationResponse = authenticationService.refreshToken(request, response);
            if (authenticationResponse != null) {
                return SuccessEntityResponse.ok("Token refreshed successfully", authenticationResponse);
            } else {
                throw new UnauthorizedException("Failed to refresh token. Please log in again.");
            }
        } catch (Exception e) {
            throw e;
        }
    }

//    @GetMapping("/send-otp-register/{email}")
//    public ResponseEntity<MessageResponse<Boolean>> sendOtpRegister(
//            @PathVariable String email
//    ){
//        UserEventPayload payload = new UserEventPayload(email,"Xác minh tài khoản");
//        emailService.sendOTPEmail(payload);
//        return SuccessEntityResponse.ok("OTP sent successfully", true);
//    }
    /*
       Nếu là otp reset password thì email + "-reset-pwd"
     */
    // @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @GetMapping("/validate-otp")
    public ResponseEntity<MessageResponse<Boolean>> validateOtp(
            @QueryParam("email") String email,
            @QueryParam("otp") String otp
    ) {
        boolean isValid = emailService.validateOTP(email, otp);
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

    @PreAuthorize("hasAnyRole('PATIENT')")
    @PostMapping("/verify-account")
    public ResponseEntity<MessageResponse<Boolean>> verifyAccount(
            @QueryParam("email") String email,
            @QueryParam("otp") String otp
    ) {
        boolean isVerified = authenticationService.verifyAccount(email, otp);
        if (isVerified) {
            return SuccessEntityResponse.ok("Account verified successfully", true);
        } else {
            MessageResponse<Boolean> response = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to verify account",
                    false,
                    isVerified
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    // @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @GetMapping("/send-otp-reset-password/{email}")
    public ResponseEntity<MessageResponse<Object>> sendOtpResetPassword(
            @PathVariable String email
    ) {
        UserEventPayload payload = new UserEventPayload(email, "Xác minh mật khẩu");
        ResetPasswordResponse response = emailService.sendOTPResetPassword(payload);
        if (response != null && response.getStatusCode() == HttpStatus.OK.value()) {
            return SuccessEntityResponse.ok("OTP sent successfully", response);
        } else {
            throw new UserNotFoundException("User not found with email: " + email);
        }
    }

    // @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR')")
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse<Boolean>> resetPassword(
            @RequestBody ResetPasswordRequest resetPasswordRequest
    ) {
        boolean isReset = authenticationService.resetPassword(resetPasswordRequest);
        if (isReset) {
            return SuccessEntityResponse.ok("Password reset successfully", true);
        } else {
            MessageResponse<Boolean> response = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to reset password",
                    false,
                    false
            );
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/doctor-account")
    public ResponseEntity<MessageResponse<DoctorResponse>> createAccountForDoctor(
            @RequestBody CreateDoctorAccountRequest request
    ) {
        return SuccessEntityResponse.ok("Create account for doctor successfully", doctorService.createAccountForDoctor(request));
    }
}
