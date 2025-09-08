package fit.iuh.student.notificationservice.consumers;

import fit.iuh.student.notificationservice.consumers.payload.MedicalRecordPayload;
import fit.iuh.student.notificationservice.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class MedicalRecordEventConsumer {
    private final EmailService emailService;
    @RabbitListener(queues = "NOTIFICATION_HEALTH_RECORD_QUEUE")
    public void handleMedicalRecordEvent(MedicalRecordPayload payload){
        try{
            String eventType = payload.getEventType();
            log.info("Notification Service received medical record event: {}", eventType);
            switch (eventType) {
                case "MEDICAL_RECORD_CREATED":
                    log.info("Processing create medical record event for appointment: {}", payload.getAppointmentId());
                    emailService.sendEmailCompleteAppointmentStatus(payload);
                    log.info("Create medical record email sent successfully for appointment: {}", payload.getAppointmentId());
                    break;
                default:
                    log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Error in processing medical record event for appointment: {}",
                    payload.getAppointmentId(), e);
            // Có thể throw exception để RabbitMQ retry
            // throw e;
        }
    }
}
