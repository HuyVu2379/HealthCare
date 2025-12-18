package fit.iuh.student.schedulingservice.dtos.requests;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleFollowUpByDoctorRequest {
    @NotNull(message = "Medical Record ID is required")
    private String medicalRecordId;

    @NotNull(message = "Patient ID is required")
    private String patientId;

    @NotNull(message = "Doctor ID is required")
    private String doctorId;

    @NotNull(message = "Schedule ID is required")
    private String scheduleId;

    @NotNull(message = "Slot ID is required")
    private Integer slotId;

    @NotNull(message = "Appointment date is required")
    private String appointmentDate;

    private String note;

    @JsonProperty("payment_method")
    private String paymentMethod;
}
