package fit.iuh.student.userservice.consumers;

import fit.iuh.student.userservice.consumers.payload.MedicalRecordPayload;
import fit.iuh.student.userservice.entities.MedicalHistory;
import fit.iuh.student.userservice.repositories.MedicalHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentEventConsumer {
    private final MedicalHistoryRepository medicalHistoryRepository;
    @RabbitListener(queues = "APPOINTMENT_NOTIFICATION_QUEUE")
    public void handleAppointmentEvent(MedicalRecordPayload message) {
        log.info("Received appointment event: {}", message);
        // Xử lý sự kiện cuộc hẹn ở đây (ví dụ: gửi email thông báo)
        try{
            medicalHistoryRepository.save(new MedicalHistory());
            log.info("Medical record saved successfully for appointment ID: {}", message.getAppointmentId());
        } catch (Exception e) {
            log.error("Error processing appointment event: {}", e.getMessage());
        }
    }
}
