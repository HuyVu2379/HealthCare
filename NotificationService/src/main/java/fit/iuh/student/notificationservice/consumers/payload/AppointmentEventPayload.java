package fit.iuh.student.notificationservice.consumers.payload;

import fit.iuh.student.notificationservice.consumers.enums.AppointmentStatus;
import fit.iuh.student.notificationservice.consumers.enums.ConsultationType;
import fit.iuh.student.notificationservice.consumers.event.AppointmentEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEventPayload {
    private String appointmentId;
    private DoctorClientResponse doctor;
    private PatientClientResponse patient;
    private String symptoms;
    private String note;
    private AppointmentStatus status;
    private TimeSlotDTO timeSlot;
    private Date appointmentDate;
    private ConsultationType consultationType;
    private String addressDetail;
    private AppointmentEvent eventType;
}