package fit.iuh.student.notificationservice.consumers.payload;

import fit.iuh.student.notificationservice.consumers.enums.AppointmentStatus;
import fit.iuh.student.notificationservice.consumers.enums.ConsultationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RescheduleAppointmentResponse {
    private String appointmentId;
    private DoctorClientResponse doctor;
    private PatientClientResponse patient;
    private String symptoms;
    private String note;
    private AppointmentStatus status = AppointmentStatus.PENDING;
    private TimeSlotDTO timeSlot;
    private Date appointmentDate;
    private ConsultationType consultationType;
    private String addressDetail;
    private DateAppointment oldAppointment;
    private DateAppointment newAppointment;
    @AllArgsConstructor
    @Builder
    @NoArgsConstructor
    @Data
    public static class DateAppointment{
        private Date appointmentDate;
        private TimeSlotDTO timeSlot;
    }
}

