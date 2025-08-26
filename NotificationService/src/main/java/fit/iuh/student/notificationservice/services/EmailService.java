package fit.iuh.student.notificationservice.services;

import fit.iuh.student.notificationservice.consumer.payload.AppointmentEventPayload;
import fit.iuh.student.notificationservice.consumer.payload.UserEventPayload;

public interface EmailService {
    void sendOtpRegisterEmail(UserEventPayload payload);
    void sendOtpResetPasswordEmail(UserEventPayload payload);
    void sendEmailBookingAppointment(AppointmentEventPayload payload);
    void sendEmailCancelAppointment(AppointmentEventPayload payload);
    void sendEmailRescheduleAppointment(AppointmentEventPayload payload);
    void sendEmailConfirmAppointmentStatus(AppointmentEventPayload payload);
    void sendEmailCompleteAppointmentStatus(AppointmentEventPayload payload);
    void sendEmailRejectAppointmentStatus(AppointmentEventPayload payload);
    void sendEmailRemindAppointment(AppointmentEventPayload payload);
}
