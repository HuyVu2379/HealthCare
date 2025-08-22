package fit.iuh.student.schedulingservice.dtos.responses;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest.TimeSlotDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TimeSlotResponse {
    private Integer slotId;
    private LocalTime startTime;
    private LocalTime endTime;
    private String message;
    private Integer totalImported;
    private List<TimeSlotDto> timeSlots;
}
