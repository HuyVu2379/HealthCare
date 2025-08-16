package fit.iuh.student.notificationservice.services;

import fit.iuh.student.notificationservice.consumer.payload.UserEventPayload;

public interface EmailService {
    void sendOtpRegisterEmail(UserEventPayload payload);
    void sendOtpResetPasswordEmail(UserEventPayload payload);
}
