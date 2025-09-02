package fit.iuh.student.schedulingservice.dtos.requests;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotRequest {
    @NotNull
    @Size(min = 1, max = 100)
    @Valid
    private List<TimeSlotDto> timeSlots;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TimeSlotDto {
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        private LocalTime startTime;

        @NotNull
        @JsonFormat(pattern = "HH:mm")
        private LocalTime endTime;
    }
}