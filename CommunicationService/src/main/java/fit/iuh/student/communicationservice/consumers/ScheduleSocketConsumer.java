package fit.iuh.student.communicationservice.consumers;
import fit.iuh.student.communicationservice.consumers.payload.AppointmentEventMessage;
import fit.iuh.student.communicationservice.handlers.CustomWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for appointment notification responses from SchedulingService
 * Listens to SCHEDULE_SOCKET_RESPONSE_QUEUE and sends WebSocket notifications to clients
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleSocketConsumer {
    private final CustomWebSocketHandler webSocketHandler;

    /**
     * Consumes AppointmentEventMessage from SchedulingService
     * This message contains AppointmentData with doctor/patient IDs
     * Then broadcasts the notification via WebSocket to Doctor and Patient
     */
    @RabbitListener(queues = "SCHEDULE_SOCKET_RESPONSE_QUEUE")
    public void handleScheduleSocketEvent(AppointmentEventMessage payload){
        log.info("Received appointment notification from RESPONSE queue: {}", payload);

        try {
            // Null safety check - Kiểm tra payload
            if (payload == null) {
                log.error("Received null payload from SCHEDULE_SOCKET_RESPONSE_QUEUE");
                return;
            }

            // Null safety check - Kiểm tra data
            if (payload.getData() == null) {
                log.error("Received message with null data from SCHEDULE_SOCKET_RESPONSE_QUEUE. EventType: {}, Payload: {}",
                         payload.getEventType(), payload);
                return;
            }

            // Gọi handleScheduleAppointment để gửi data đến client qua WebSocket
            webSocketHandler.handlePublishScheduleToClient(payload.getData());
            log.info("Successfully sent appointment notification via WebSocket. EventType: {}, DoctorId: {}, PatientId: {}",
                     payload.getData().getEventType(),
                     payload.getData().getDoctorId(),
                     payload.getData().getPatientId());
        } catch (Exception e) {
            log.error("Error while handling appointment notification WebSocket event: ", e);
        }
    }
}
