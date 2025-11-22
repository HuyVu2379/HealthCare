package fit.iuh.student.communicationservice.publishers.payload;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @JsonProperty("payment_method")
    private String paymentMethod;
}
