package fit.iuh.student.schedulingservice.dtos.requests;

import fit.iuh.student.schedulingservice.enums.WeekDay;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BulkCreateScheduleRequest {
    private String doctorId;
    private List<DateScheduleDTO> dateSchedules;
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DateScheduleDTO{
        private WeekDay weekDay;
        private Date workDate;
    }
}
