package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotResponse {
    private String message;
    private int totalImported;
    private List<TimeSlotRequest.TimeSlotDto> timeSlots;
}
