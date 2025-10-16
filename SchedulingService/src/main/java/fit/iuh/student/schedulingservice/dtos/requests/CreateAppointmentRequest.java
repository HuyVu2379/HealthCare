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
    private String patientId;
    private String scheduleId;
    private String doctorId;
    private String symptoms;
    private String note;
    private int slotId;
    private AppointmentStatus status = AppointmentStatus.PENDING;
    private ConsultationType consultationType;
    private String addressDetail;
    private Boolean hasPredict;
}
