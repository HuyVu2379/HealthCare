package fit.iuh.student.schedulingservice.publishers;
import fit.iuh.student.schedulingservice.publishers.payload.AppointmentData;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ScheduleSocketPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String SCHEDULE_SOCKET_QUEUE = "SCHEDULE_SOCKET_QUEUE";
    public void publishAppointmentList(AppointmentData data) {
        try {
            log.info("Publishing appointment list: {}", data);
            AppointmentEventPublisher.AppointmentEventMessage message = new  AppointmentEventPublisher.AppointmentEventMessage(data, "APPOINTMENT_SOCKET_LIST");
            rabbitTemplate.convertAndSend(SCHEDULE_SOCKET_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing schedule socket event", e);
        }
    }
}
