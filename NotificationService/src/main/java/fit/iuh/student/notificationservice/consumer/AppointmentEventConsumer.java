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
            
            if (eventType.equals("BOOKING_APPOINTMENT")) {
                payload.setEventType(AppointmentEvent.BOOKING_APPOINTMENT);
                log.info("Processing booking appointment event for patient: {}", payload.getPatient().getFullName());
                emailService.sendEmailBookingAppointment(payload);
                log.info("Booking appointment email sent successfully for appointment: {}", payload.getAppointmentId());
            } else if (eventType.equals("CANCEL_APPOINTMENT")) {
                payload.setEventType(AppointmentEvent.CANCEL_APPOINTMENT);
                log.info("Processing cancelled appointment event for patient: {}", payload.getPatient().getFullName());
                emailService.sendEmailCancelAppointment(payload);
                log.info("Cancel appointment email sent successfully for appointment: {}", payload.getAppointmentId());
            } else {
                log.warn("Unknown appointment event type: {}", eventType);
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
