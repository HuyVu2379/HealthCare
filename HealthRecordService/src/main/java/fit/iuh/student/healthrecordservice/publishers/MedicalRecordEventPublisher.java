package fit.iuh.student.healthrecordservice.publishers;

import fit.iuh.student.healthrecordservice.publishers.payload.MedicalRecordPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class MedicalRecordEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String MEDICAL_RECORD_QUEUE = "MEDICAL_RECORD_QUEUE";

    public void publishCreateMedicalRecordEvent(MedicalRecordPayload payload){
        try {
            rabbitTemplate.convertAndSend(MEDICAL_RECORD_QUEUE, payload);
        } catch (Exception e) {
            log.error("Error in publishing booking appointment event", e);
        }
    }
}
