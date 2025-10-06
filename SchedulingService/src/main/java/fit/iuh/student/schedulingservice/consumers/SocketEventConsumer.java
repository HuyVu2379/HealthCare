package fit.iuh.student.schedulingservice.consumers;

import fit.iuh.student.schedulingservice.consumers.payload.ScheduleEventMessage;
import fit.iuh.student.schedulingservice.consumers.payload.ScheduleSocketEvent;
import fit.iuh.student.schedulingservice.publishers.ScheduleSocketPublisher;
import fit.iuh.student.schedulingservice.publishers.payload.AppointmentData;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class SocketEventConsumer {
    private final AppointmentService appointmentService;
    private final ScheduleSocketPublisher scheduleSocketPublisher;

    @RabbitListener(queues = "SCHEDULE_SOCKET_QUEUE")
    public void handleScheduleSocketEvent(ScheduleEventMessage scheduleEventMessage) {
        ScheduleSocketEvent eventType = scheduleEventMessage.getEvent();
        switch (eventType) {
            case BOOKING_APPOINTMENT -> {
                log.info("Handling booking appointment event: {}", scheduleEventMessage);
                appointmentService.bookingAppointment(scheduleEventMessage.getCreateAppointmentRequest());
                scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                        .eventType("BOOKING_APPOINTMENT").doctorId(scheduleEventMessage.getDoctorId())
                        .appointmentId(scheduleEventMessage.getAppointmentId())
                        .patientId(scheduleEventMessage.getPatientId())
                        .success(true).build());
            }
            case UPDATE_APPOINTMENT_STATUS -> {
                log.info("Handling update appointment event: {}", scheduleEventMessage);
                appointmentService.updateAppointmentStatus(scheduleEventMessage.getAppointmentId(), scheduleEventMessage.getStatus());
                scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                        .eventType("UPDATE_APPOINTMENT_STATUS").doctorId(scheduleEventMessage.getDoctorId())
                        .appointmentId(scheduleEventMessage.getAppointmentId())
                        .patientId(scheduleEventMessage.getPatientId())
                        .success(true).build());            }
            case RESCHEDULE_APPOINTMENT -> {
                log.info("Handling reschedule appointment event: {}", scheduleEventMessage);
                appointmentService.rescheduleAppointment(scheduleEventMessage.getUpdateAppointmentRequest());
                scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                        .eventType("RESCHEDULE_APPOINTMENT").doctorId(scheduleEventMessage.getDoctorId())
                        .appointmentId(scheduleEventMessage.getAppointmentId())
                        .patientId(scheduleEventMessage.getPatientId())
                        .success(true).build());
            }
            case CANCEL_APPOINTMENT -> {
                log.info("Handling cancel appointment event: {}", scheduleEventMessage);
                appointmentService.cancelAppointment(scheduleEventMessage.getAppointmentId(), scheduleEventMessage.getPatientId());
                scheduleSocketPublisher.publishAppointmentList(AppointmentData.builder()
                        .eventType("CANCEL_APPOINTMENT").doctorId(scheduleEventMessage.getDoctorId())
                        .appointmentId(scheduleEventMessage.getAppointmentId())
                        .patientId(scheduleEventMessage.getPatientId())
                        .success(true).build());
            }
        }
    }
}
