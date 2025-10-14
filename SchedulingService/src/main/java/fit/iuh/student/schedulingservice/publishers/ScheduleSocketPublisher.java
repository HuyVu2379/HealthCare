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

    /**
     * Queue name for sending appointment notification responses to CommunicationService
     * SchedulingService (this service) publishes AppointmentEventMessage to this queue
     * CommunicationService will consume and send WebSocket notifications to clients
     */
    private static final String SCHEDULE_SOCKET_RESPONSE_QUEUE = "SCHEDULE_SOCKET_RESPONSE_QUEUE";

    public void publishAppointmentList(AppointmentData data) {
        try {
            log.info("Publishing appointment notification to RESPONSE queue: {}", data);
            AppointmentEventPublisher.AppointmentEventMessage message = new AppointmentEventPublisher.AppointmentEventMessage(data, "APPOINTMENT_SOCKET_LIST");
            rabbitTemplate.convertAndSend(SCHEDULE_SOCKET_RESPONSE_QUEUE, message);
            log.info("Successfully published to queue: {} - DoctorId: {}, PatientId: {}",
                    SCHEDULE_SOCKET_RESPONSE_QUEUE, data.getDoctorId(), data.getPatientId());
        } catch (Exception e) {
            log.error("Error in publishing appointment notification to RESPONSE queue", e);
        }
    }
}
