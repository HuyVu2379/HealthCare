package fit.iuh.student.communicationservice.consumers;
import fit.iuh.student.communicationservice.consumers.payload.AppointmentEventMessage;
import fit.iuh.student.communicationservice.handlers.CustomWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ScheduleSocketConsumer {
    private final CustomWebSocketHandler webSocketHandler;
    @RabbitListener(queues = "SCHEDULE_SOCKET_QUEUE")
    public void handleScheduleSocketEvent(AppointmentEventMessage payload){
        log.info("Received schedule socket event: {}", payload);

        try {
            // Gọi handleScheduleAppointment để gửi data đến client qua WebSocket
            webSocketHandler.handlePublishScheduleToClient(payload.getData());
            log.info("Successfully sent schedule appointment data to clients via WebSocket");
        } catch (Exception e) {
            log.error("Error while handling schedule appointment WebSocket event: ", e);
        }
    }
}
