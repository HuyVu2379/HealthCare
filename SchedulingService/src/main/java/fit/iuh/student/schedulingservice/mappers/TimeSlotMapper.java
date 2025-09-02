package fit.iuh.student.schedulingservice.mappers;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest.TimeSlotDto;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotDTO;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotResponse;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TimeSlotMapper {
    @Mapping(source = "slotId", target = "slotId")
    @Mapping(source = "startTime", target = "startTime")
    @Mapping(source = "endTime", target = "endTime")
    TimeSlotResponse timeSlotToResponse(TimeSlot timeSlot);
    
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    TimeSlot toEntity(TimeSlotDto dto);
    
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    TimeSlotDto toDto(TimeSlot entity);

    @Mapping(target = "slotId", source = "slotId")
    @Mapping(target = "startTime", source = "startTime")
    @Mapping(target = "endTime", source = "endTime")
    TimeSlotDTO toTimeSlotDTO(TimeSlot entity);

}