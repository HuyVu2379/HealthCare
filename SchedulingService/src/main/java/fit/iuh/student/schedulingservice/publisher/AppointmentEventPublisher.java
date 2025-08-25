package fit.iuh.student.schedulingservice.publisher;

import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
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

    // Inner class để wrap message với eventType
    public static class AppointmentEventMessage {
        private AppointmentResponse payload;
        private String eventType;

        public AppointmentEventMessage() {}

        public AppointmentEventMessage(AppointmentResponse payload, String eventType) {
            this.payload = payload;
            this.eventType = eventType;
        }

        public AppointmentResponse getPayload() { return payload; }
        public void setPayload(AppointmentResponse payload) { this.payload = payload; }
        public String getEventType() { return eventType; }
        public void setEventType(String eventType) { this.eventType = eventType; }
    }
}
