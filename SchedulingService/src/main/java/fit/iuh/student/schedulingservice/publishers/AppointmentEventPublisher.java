package fit.iuh.student.schedulingservice.publishers;

import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.RescheduleAppointmentResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String APPOINTMENT_NOTIFICATION_QUEUE = "APPOINTMENT_NOTIFICATION_QUEUE";
    
    public void publishBookingAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing booking appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "BOOKING_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing booking appointment event", e);
        }
    }
    
    public void publishCancelledAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing cancelled appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "CANCEL_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing cancelled appointment event", e);
        }
    }

    public void publishRescheduleAppointmentEvent(RescheduleAppointmentResponse payload) {
        try {
            log.info("Publishing rescheduled appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "RESCHEDULE_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing rescheduled appointment event", e);
        }
    }

    public void publishConfirmStatusAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing update status appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "CONFIRM_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing update status appointment event", e);
        }
    }
    
//    public void publishCompletedStatusAppointmentEvent(AppointmentResponse payload) {
//        try {
//            log.info("Publishing update status appointment event for patient: {}", payload.getPatient().getFullName());
//            // Tạo message wrapper với eventType
//            AppointmentEventMessage message = new AppointmentEventMessage(payload, "COMPLETED_APPOINTMENT");
//            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
//        } catch (Exception e) {
//            log.error("Error in publishing update status appointment event", e);
//        }
//    }
    
    public void publishNoShowStatusAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing update status appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "NO_SHOW_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing update status appointment event", e);
        }
    }
    
    /**
     * Publish appointment reminder event
     * This method is called by the AppointmentReminderJob to send reminders 12 hours before appointments
     */
    public void publishAppointmentReminderEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing appointment reminder event for patient: {}", payload.getPatient().getFullName());
            // Create message wrapper with eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "APPOINTMENT_REMINDER");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing appointment reminder event", e);
        }
    }
    // Inner class để wrap message với eventType
    public static class AppointmentEventMessage {
        private AppointmentResponse payload;
        private String eventType;
        private RescheduleAppointmentResponse rescheduleAppointmentResponse;

        public AppointmentEventMessage() {}

        public AppointmentEventMessage(AppointmentResponse payload, String eventType) {
            this.payload = payload;
            this.eventType = eventType;
        }
        public AppointmentEventMessage(RescheduleAppointmentResponse rescheduleAppointmentResponse, String eventType) {
            this.rescheduleAppointmentResponse = rescheduleAppointmentResponse;
            this.eventType = eventType;
        }
        public AppointmentResponse getPayload() { return payload; }
        public RescheduleAppointmentResponse getReSchedulePayload() { return rescheduleAppointmentResponse; }
        public void setPayload(AppointmentResponse payload) { this.payload = payload; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
