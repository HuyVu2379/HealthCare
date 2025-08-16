package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.responses.ResetPasswordResponse;
import fit.iuh.student.userservice.publisher.payload.UserEventPayload;

public interface EmailService {
    void sendOTPEmail(UserEventPayload payload);
    boolean validateOTP(String email, String otp);
    ResetPasswordResponse sendOTPResetPassword(UserEventPayload payload);
}
