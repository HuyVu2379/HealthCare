package fit.iuh.student.schedulingservice.consumers;

import fit.iuh.student.schedulingservice.consumers.payload.MedicalRecordPayload;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
@Slf4j
@RequiredArgsConstructor
public class MedicalRecordEventConsumer {
    private final AppointmentRepository appointmentRepository;
    @RabbitListener(queues = "MEDICAL_RECORD_QUEUE")
    public void handleCreateMedicalRecordEvent(MedicalRecordPayload payload){
        if(Objects.equals(payload.getEventType(), "CREATE_MEDICAL_RECORD")){
            log.info("Received create medical record event: {}", payload);
            appointmentRepository.updateAppointmentStatusById(payload.getAppointmentId(), AppointmentStatus.COMPLETED);
        }
    }
}
