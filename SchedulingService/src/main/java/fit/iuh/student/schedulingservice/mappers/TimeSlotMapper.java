package fit.iuh.student.schedulingservice.mappers;
import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import org.springframework.stereotype.Component;

@Component
public class TimeSlotMapper {
    public TimeSlot convertToEntity(TimeSlotRequest.TimeSlotDto dto) {
        TimeSlot timeSlot = new TimeSlot();
        timeSlot.setStartTime(dto.getStartTime());
        timeSlot.setEndTime(dto.getEndTime());
        return timeSlot;
    }
    
    public TimeSlotRequest.TimeSlotDto convertToDto(TimeSlot entity) {
        TimeSlotRequest.TimeSlotDto dto = new TimeSlotRequest.TimeSlotDto();
        dto.setStartTime(entity.getStartTime());
        dto.setEndTime(entity.getEndTime());
        return dto;
    }
}
