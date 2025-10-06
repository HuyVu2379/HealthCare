package fit.iuh.student.communicationservice.publishers.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {
    private String appointmentId;
    private String oldScheduleId;
    private String newScheduleId;
    private Integer oldSlotId;
    private Integer newSlotId;
}
