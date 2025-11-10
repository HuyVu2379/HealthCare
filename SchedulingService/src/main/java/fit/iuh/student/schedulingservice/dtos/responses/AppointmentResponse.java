package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.clients.dtos.PatientClientResponse;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import fit.iuh.student.schedulingservice.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private String appointmentId;
    private String patientId;
    private String patientName;
    private String doctorId;
    private String doctorName;
    private String specialty;
    private DoctorClientResponse doctor;
    private PatientClientResponse patient;
    private String symptoms;
    private String note;
    private Integer slotId;
    private AppointmentStatus status = AppointmentStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    private TimeSlotDTO timeSlot;
    private Date appointmentDate;
    private ConsultationType consultationType;
    private String addressDetail;
    private boolean hasPredict = false;
    private String relatedRecordId; // Medical Record ID mà lịch tái khám này đến từ
}
