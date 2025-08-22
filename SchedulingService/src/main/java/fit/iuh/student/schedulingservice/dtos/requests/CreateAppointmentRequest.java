package fit.iuh.student.schedulingservice.dtos.requests;

import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAppointmentRequest {
    private int patientId;
    private String scheduleId;
    private int doctorId;
    private String symptoms;
    private String note;
    private int slotId;
    private AppointmentStatus status = AppointmentStatus.PENDING;
    private Date appointmentDate;
    private ConsultationType consultationType;
    private String addressDetail;
}
