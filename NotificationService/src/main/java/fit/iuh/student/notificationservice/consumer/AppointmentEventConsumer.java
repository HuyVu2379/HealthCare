package fit.iuh.student.notificationservice.consumer;

import fit.iuh.student.notificationservice.consumer.event.AppointmentEvent;
import fit.iuh.student.notificationservice.consumer.payload.AppointmentEventPayload;
import fit.iuh.student.notificationservice.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentEventConsumer {
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(AppointmentEventConsumer.class);
    
    @RabbitListener(queues = "APPOINTMENT_NOTIFICATION_QUEUE")
    public void handleAppointmentEvent(AppointmentEventMessage message){
        try{
            String eventType = message.getEventType();
            AppointmentEventPayload payload = message.getPayload();

            log.info("Received appointment event: {}", eventType);
            
            switch (eventType) {
                case "BOOKING_APPOINTMENT":
                    payload.setEventType(AppointmentEvent.BOOKING_APPOINTMENT);
                    log.info("Processing booking appointment event for patient: {}", payload.getPatient().getFullName());
                    emailService.sendEmailBookingAppointment(payload);
                    log.info("Booking appointment email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                case "CANCEL_APPOINTMENT":
                    payload.setEventType(AppointmentEvent.CANCEL_APPOINTMENT);
                    log.info("Processing cancelled appointment event for patient: {}", payload.getPatient().getFullName());
                    emailService.sendEmailCancelAppointment(payload);
                    log.info("Cancel appointment email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                case "APPOINTMENT_REMINDER":
                    payload.setEventType(AppointmentEvent.APPOINTMENT_REMINDER);
                    log.info("Processing appointment reminder event for patient: {}", payload.getPatient().getFullName());
                    emailService.sendEmailRemindAppointment(payload);
                    log.info("Appointment reminder email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                case "RESCHEDULE_APPOINTMENT":
                    payload.setEventType(AppointmentEvent.RESCHEDULE_APPOINTMENT);
                    log.info("Processing rescheduled appointment event for patient: {}", payload.getPatient().getFullName());
//                    emailService.sendEmailRescheduleAppointment(payload);
                    log.info("Reschedule appointment email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                case "CONFIRM_APPOINTMENT":
                    payload.setEventType(AppointmentEvent.CONFIRM_APPOINTMENT);
                    log.info("Processing confirm appointment event for patient: {}", payload.getPatient().getFullName());
                    emailService.sendEmailConfirmAppointmentStatus(payload);
                    log.info("Confirm appointment email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                case "COMPLETED_APPOINTMENT":
                    payload.setEventType(AppointmentEvent.COMPLETED_APPOINTMENT);
                    log.info("Processing completed appointment event for patient: {}", payload.getPatient().getFullName());
                    emailService.sendEmailCompleteAppointmentStatus(payload);
                    log.info("Completed appointment email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                case "NO_SHOW_APPOINTMENT":
                    payload.setEventType(AppointmentEvent.NO_SHOW_APPOINTMENT);
                    log.info("Processing no-show appointment event for patient: {}", payload.getPatient().getFullName());
                    emailService.sendEmailRejectAppointmentStatus(payload);
                    log.info("No-show appointment email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                    
                default:
                    log.warn("Unknown appointment event type: {}", eventType);
                    break;
            }
        } catch (Exception e){
            logger.error("Error processing appointment event", e);
        }
    }
    
    // Inner class để nhận message từ SchedulingService
    public static class AppointmentEventMessage {
        private AppointmentEventPayload payload;
        private String eventType;
        
        public AppointmentEventMessage() {}
        
        public AppointmentEventPayload getPayload() { return payload; }
        public void setPayload(AppointmentEventPayload payload) { this.payload = payload; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
