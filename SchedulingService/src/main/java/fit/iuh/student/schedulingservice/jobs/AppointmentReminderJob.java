package fit.iuh.student.schedulingservice.jobs;

import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.publisher.AppointmentEventPublisher;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppointmentReminderJob implements Job {

    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;
    private final AppointmentEventPublisher eventPublisher;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing appointment reminder job at {}", LocalDateTime.now());
        
        try {
            // Find appointments that need reminders (12 hours before appointment time)
            List<Appointment> appointmentsToRemind = appointmentRepository.findAppointmentsForReminder();
            
            log.info("Found {} appointments that need reminders", appointmentsToRemind.size());
            
            // Send reminder for each appointment
            for (Appointment appointment : appointmentsToRemind) {
                try {
                    // Get appointment details
                    AppointmentResponse appointmentResponse = appointmentService.getAppointmentDetailById(appointment.getAppointmentId());
                    
                    // Publish reminder event
                    eventPublisher.publishAppointmentReminderEvent(appointmentResponse);
                    
                    log.info("Sent reminder for appointment: {}", appointment.getAppointmentId());
                } catch (Exception e) {
                    log.error("Error sending reminder for appointment: {}", appointment.getAppointmentId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Error executing appointment reminder job", e);
            throw new JobExecutionException(e);
        }
    }
}