package fit.iuh.student.schedulingservice.consumers;

import fit.iuh.student.schedulingservice.consumers.payload.MedicalRecordPayload;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.PaymentStatus;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
@RequiredArgsConstructor
public class MedicalRecordEventConsumer {
    private final AppointmentRepository appointmentRepository;
    
    @RabbitListener(queues = "SCHEDULE_HEALTH_RECORD_QUEUE")
    @Transactional
    public void handleCreateMedicalRecordEvent(MedicalRecordPayload payload){
        if(payload.getEventType().equals("MEDICAL_RECORD_CREATED")){
            log.info("Received create medical record event: {}", payload);
            
            // Cập nhật appointment status thành COMPLETED
            appointmentRepository.updateAppointmentStatusById(payload.getAppointmentId(), AppointmentStatus.COMPLETED);
            
            // Lấy appointment để kiểm tra payment method và payment status
            Appointment appointment = appointmentRepository.findById(payload.getAppointmentId())
                    .orElse(null);
            
            if (appointment != null) {
                // Nếu payment_method là CASH và payment_status là UNPAID
                // thì tự động cập nhật payment_status thành PAID
                // (vì bác sĩ đã hoàn thành khám = bệnh nhân đã đóng tiền)
                if ("CASH".equals(appointment.getPaymentMethod()) 
                    && appointment.getPaymentStatus() == PaymentStatus.UNPAID) {
                    
                    appointment.setPaymentStatus(PaymentStatus.PAID);
                    appointmentRepository.save(appointment);
                    
                    log.info("Auto-updated payment_status to PAID for CASH payment appointment: {}", 
                            payload.getAppointmentId());
                }
            } else {
                log.warn("Appointment not found for ID: {}", payload.getAppointmentId());
            }
        }
    }
}
