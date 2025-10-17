package fit.iuh.student.schedulingservice.consumers;

import fit.iuh.student.schedulingservice.consumers.payload.ScheduleEventMessage;
import fit.iuh.student.schedulingservice.consumers.payload.ScheduleSocketEvent;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.RescheduleAppointmentResponse;
import fit.iuh.student.schedulingservice.publishers.ScheduleSocketPublisher;
import fit.iuh.student.schedulingservice.publishers.payload.AppointmentData;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Consumer for appointment schedule requests from CommunicationService
 * Listens to SCHEDULE_SOCKET_REQUEST_QUEUE and processes appointment operations
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SocketEventConsumer {
    private final AppointmentService appointmentService;
    private final ScheduleSocketPublisher scheduleSocketPublisher;

    /**
     * Consumes ScheduleEventMessage from CommunicationService
     * Processes appointment booking/update/cancel/reschedule
     * Then publishes result to SCHEDULE_SOCKET_RESPONSE_QUEUE for WebSocket notification
     */
    @RabbitListener(queues = "SCHEDULE_SOCKET_REQUEST_QUEUE")
    public void handleScheduleSocketEvent(ScheduleEventMessage scheduleEventMessage) {
        log.info("Received appointment request from REQUEST queue: {}", scheduleEventMessage);

        try {
            ScheduleSocketEvent eventType = scheduleEventMessage.getEvent();
            switch (eventType) {
                case BOOKING_APPOINTMENT -> handleBookingAppointment(scheduleEventMessage);
                case UPDATE_APPOINTMENT_STATUS -> handleUpdateAppointmentStatus(scheduleEventMessage);
                case RESCHEDULE_APPOINTMENT -> handleRescheduleAppointment(scheduleEventMessage);
                case CANCEL_APPOINTMENT -> handleCancelAppointment(scheduleEventMessage);
                default -> log.warn("Unknown event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Unexpected error handling schedule event: {}", scheduleEventMessage, e);
            // Don't throw - prevent RabbitMQ from retrying indefinitely
        }
    }

    private void handleBookingAppointment(ScheduleEventMessage scheduleEventMessage) {
        log.info("Handling booking appointment event: {}", scheduleEventMessage);
        try {
            // Try to book appointment
            AppointmentResponse apt = appointmentService.bookingAppointment(scheduleEventMessage.getCreateAppointmentRequest());

            // Success - publish success notification
            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("BOOKING_APPOINTMENT")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(apt.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(true)
                    .build());
            log.info("Booking appointment completed successfully and notification sent to RESPONSE queue");

        } catch (Exception e) {
            // Failed - publish failure notification
            log.error("Booking appointment failed - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("BOOKING_APPOINTMENT_FAILED")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(scheduleEventMessage.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(false)
                    .build());
            log.info("Published FAILURE notification to RESPONSE queue - Reason: {}", e.getMessage());
            // Don't throw - message will be acknowledged and not retried
        }
    }

    private void handleUpdateAppointmentStatus(ScheduleEventMessage scheduleEventMessage) {
        log.info("Handling update appointment event: {}", scheduleEventMessage);
        try {
            AppointmentResponse apt = appointmentService.updateAppointmentStatus(scheduleEventMessage.getAppointmentId(), scheduleEventMessage.getStatus());

            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("UPDATE_APPOINTMENT_STATUS")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(apt.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(true)
                    .build());
            log.info("Update appointment completed and notification sent to RESPONSE queue");

        } catch (Exception e) {
            log.error("Update appointment failed - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("UPDATE_APPOINTMENT_STATUS_FAILED")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(scheduleEventMessage.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(false)
                    .build());
        }
    }

    private void handleRescheduleAppointment(ScheduleEventMessage scheduleEventMessage) {
        log.info("Handling reschedule appointment event: {}", scheduleEventMessage);
        try {
            RescheduleAppointmentResponse apt=appointmentService.rescheduleAppointment(scheduleEventMessage.getUpdateAppointmentRequest());

            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("RESCHEDULE_APPOINTMENT")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(apt.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(true)
                    .build());
            log.info("Reschedule appointment completed and notification sent to RESPONSE queue");

        } catch (Exception e) {
            log.error("Reschedule appointment failed - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("RESCHEDULE_APPOINTMENT_FAILED")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(scheduleEventMessage.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(false)
                    .build());
        }
    }

    private void handleCancelAppointment(ScheduleEventMessage scheduleEventMessage) {
        log.info("Handling cancel appointment event: {}", scheduleEventMessage);
        try {
            appointmentService.cancelAppointment(scheduleEventMessage.getAppointmentId(), scheduleEventMessage.getPatientId());

            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("CANCEL_APPOINTMENT")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(scheduleEventMessage.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(true)
                    .build());
            log.info("Cancel appointment completed and notification sent to RESPONSE queue");

        } catch (Exception e) {
            log.error("Cancel appointment failed - {}: {}", e.getClass().getSimpleName(), e.getMessage());
            scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                    .eventType("CANCEL_APPOINTMENT_FAILED")
                    .doctorId(scheduleEventMessage.getDoctorId())
                    .appointmentId(scheduleEventMessage.getAppointmentId())
                    .patientId(scheduleEventMessage.getPatientId())
                    .success(false)
                    .build());
        }
    }
}
