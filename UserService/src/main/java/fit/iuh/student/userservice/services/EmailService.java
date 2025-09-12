package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.responses.ResetPasswordResponse;
import fit.iuh.student.userservice.publishers.payload.UserEventPayload;

public interface EmailService {
    void sendOTPEmail(UserEventPayload payload);
    boolean validateOTP(String email, String otp);
    boolean validateOTPForPasswordReset(String email, String otp);
    ResetPasswordResponse sendOTPResetPassword(UserEventPayload payload);
}
