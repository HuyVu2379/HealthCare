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

    /**
     * Queue name for sending appointment schedule requests to SchedulingService
     * CommunicationService (this service) publishes ScheduleEventMessage to this queue
     * SchedulingService will consume and process the appointment booking request
     */
    private static final String SCHEDULE_SOCKET_REQUEST_QUEUE = "SCHEDULE_SOCKET_REQUEST_QUEUE";

    public void publishScheduleEventSocket(ScheduleEventMessage scheduleEventMessage) {
        log.info("Publishing schedule event to REQUEST queue: {}", scheduleEventMessage);
        rabbitTemplate.convertAndSend(SCHEDULE_SOCKET_REQUEST_QUEUE, scheduleEventMessage);
        log.info("Successfully published to queue: {}", SCHEDULE_SOCKET_REQUEST_QUEUE);
    }
}
