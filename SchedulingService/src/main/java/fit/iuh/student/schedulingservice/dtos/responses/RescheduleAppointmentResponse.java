package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.clients.dtos.PatientClientResponse;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
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
    public static class DateAppointment{
        private Date appointmentDate;
        private TimeSlotDTO timeSlot;
    }
}
