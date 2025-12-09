package fit.iuh.student.adminservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private String appointmentId;
    private String patientId;
    private String doctorId;
    private Date appointmentDate;
    private String status;
    private String consultationType;
    private String paymentStatus;
    private LocalDateTime createdAt;
}
