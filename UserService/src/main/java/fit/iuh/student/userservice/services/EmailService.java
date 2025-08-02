package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.responses.ResetPasswordResponse;

public interface EmailService {
    void sendOTPEmail(String to, String subject);
    boolean validateOTP(String email, String otp);
    ResetPasswordResponse sendOTPResetPassword(String to, String subject);
}
