package fit.iuh.student.userservice.consumers;

import fit.iuh.student.userservice.clients.AppointmentClient;
import fit.iuh.student.userservice.clients.dtos.AppointmentClientResponse;
import fit.iuh.student.userservice.consumers.payload.MedicalRecordPayload;
import fit.iuh.student.userservice.entities.MedicalHistory;
import fit.iuh.student.userservice.enums.StatusHealth;
import fit.iuh.student.userservice.repositories.DoctorRepository;
import fit.iuh.student.userservice.repositories.MedicalHistoryRepository;
import fit.iuh.student.userservice.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppointmentEventConsumer {
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentClient appointmentClient;
    @RabbitListener(queues = "APPOINTMENT_NOTIFICATION_QUEUE")
    public void handleAppointmentEvent(MedicalRecordPayload message) {
        AppointmentClientResponse appointmentDetails = appointmentClient.getAppointmentDetailForClientById(message.getAppointmentId());
        log.info("Received appointment event: {}", message);
        // Xử lý sự kiện cuộc hẹn ở đây (ví dụ: gửi email thông báo)
        try{
            if(message.getEventType().equals("MEDICAL_RECORD_CREATED")) {
                medicalHistoryRepository.save(
                        MedicalHistory.builder()
                                .patient(patientRepository.findById(appointmentDetails.getPatientId()).get())
                                .doctor(doctorRepository.findById(appointmentDetails.getDoctorId()).get())
                                .serviceName(message.getServiceName())
                                .stage(message.getStage())
                                .statusHealth(StatusHealth.valueOf(message.getStatusHealth()))
                                .diagnosis(message.getDiagnosis())
                                .diagnosisDate(message.getDateDiagnosis().toLocalDate())
                                .notes(message.getDoctorNote())
                                .build()
                );
            }
            log.info("Medical record saved successfully for appointment ID: {}", message.getAppointmentId());
        } catch (Exception e) {
            log.error("Error processing appointment event: {}", e.getMessage());
        }
    }
}
