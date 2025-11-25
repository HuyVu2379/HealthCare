package fit.iuh.student.schedulingservice.publishers;

import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.RescheduleAppointmentResponse;
import fit.iuh.student.schedulingservice.publishers.payload.AppointmentData;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String APPOINTMENT_NOTIFICATION_QUEUE = "APPOINTMENT_NOTIFICATION_QUEUE";
    private static final String SCHEDULE_SOCKET_RESPONSE_QUEUE = "SCHEDULE_SOCKET_RESPONSE_QUEUE";
    
    public void publishBookingAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing booking appointment event for patient: {}", payload.getPatient().getFullName());

            // Gửi email notification qua APPOINTMENT_NOTIFICATION_QUEUE
            AppointmentEventMessage emailMessage = new AppointmentEventMessage(payload, "BOOKING_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, emailMessage);
            log.info("Sent email notification to APPOINTMENT_NOTIFICATION_QUEUE");

            // Gửi WebSocket notification qua SCHEDULE_SOCKET_RESPONSE_QUEUE
            AppointmentData websocketData = AppointmentData.builder()
                    .appointmentId(payload.getAppointmentId())
                    .doctorId(payload.getDoctor().getDoctorId())
                    .patientId(payload.getPatient().getUserId())
                    .eventType("BOOKING_APPOINTMENT")
                    .success(true)
                    .build();
            AppointmentEventMessage websocketMessage = new AppointmentEventMessage(websocketData, "BOOKING_APPOINTMENT");
            rabbitTemplate.convertAndSend(SCHEDULE_SOCKET_RESPONSE_QUEUE, websocketMessage);
            log.info("Sent WebSocket notification to SCHEDULE_SOCKET_RESPONSE_QUEUE for doctorId: {}, patientId: {}",
                    payload.getDoctor().getDoctorId(), payload.getPatient().getUserId());

        } catch (Exception e) {
            log.error("Error in publishing booking appointment event", e);
        }
    }
    
    public void publishCancelledAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing cancelled appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "CANCEL_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing cancelled appointment event", e);
        }
    }

    public void publishRescheduleAppointmentEvent(RescheduleAppointmentResponse payload) {
        try {
            log.info("Publishing rescheduled appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "RESCHEDULE_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing rescheduled appointment event", e);
        }
    }

    public void publishConfirmStatusAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing update status appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "CONFIRM_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing update status appointment event", e);
        }
    }

//    public void publishCompletedStatusAppointmentEvent(AppointmentResponse payload) {
//        try {
//            log.info("Publishing update status appointment event for patient: {}", payload.getPatient().getFullName());
//            // Tạo message wrapper với eventType
//            AppointmentEventMessage message = new AppointmentEventMessage(payload, "COMPLETED_APPOINTMENT");
//            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
//        } catch (Exception e) {
//            log.error("Error in publishing update status appointment event", e);
//        }
//    }

    public void publishNoShowStatusAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing update status appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "NO_SHOW_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing update status appointment event", e);
        }
    }
    
    public void publishRejectStatusAppointmentEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing reject appointment event for patient: {}", payload.getPatient().getFullName());
            // Tạo message wrapper với eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "REJECT_APPOINTMENT");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing reject appointment event", e);
        }
    }
    
    /**
     * Publish appointment reminder event
     * This method is called by the AppointmentReminderJob to send reminders 12 hours before appointments
     */
    public void publishAppointmentReminderEvent(AppointmentResponse payload) {
        try {
            log.info("Publishing appointment reminder event for patient: {}", payload.getPatient().getFullName());
            // Create message wrapper with eventType
            AppointmentEventMessage message = new AppointmentEventMessage(payload, "APPOINTMENT_REMINDER");
            rabbitTemplate.convertAndSend(APPOINTMENT_NOTIFICATION_QUEUE, message);
        } catch (Exception e) {
            log.error("Error in publishing appointment reminder event", e);
        }
    }
    // Inner class để wrap message với eventType
    @Data
    public static class AppointmentEventMessage {
        private AppointmentResponse payload;
        private RescheduleAppointmentResponse rescheduleAppointmentResponse;
        private String eventType;
        private AppointmentData data;

        public AppointmentEventMessage(AppointmentData data, String eventType) {
            this.data = data;
            this.eventType = eventType;
        }

        public AppointmentEventMessage() {}

        public AppointmentEventMessage(AppointmentResponse payload, String eventType) {
            this.payload = payload;
            this.eventType = eventType;
        }

        public AppointmentEventMessage(RescheduleAppointmentResponse rescheduleAppointmentResponse, String eventType) {
            this.rescheduleAppointmentResponse = rescheduleAppointmentResponse;
            this.eventType = eventType;
        }
    }
}
