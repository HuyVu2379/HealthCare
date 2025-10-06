package fit.iuh.student.communicationservice.publishers;

import fit.iuh.student.communicationservice.publishers.payload.ScheduleEventMessage;
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

    public void publishScheduleEventSocket(ScheduleEventMessage scheduleEventMessage) {
        log.info("Publishing schedule socket event: {}", scheduleEventMessage);
        rabbitTemplate.convertAndSend(SCHEDULE_SOCKET_QUEUE, scheduleEventMessage);
    }
}
