package fit.iuh.student.schedulingservice.mappers;

import fit.iuh.student.schedulingservice.dtos.responses.DoctorScheduleResponse;
import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DoctorScheduleMapper {
    @Mapping(source = "scheduleId", target = "scheduleId")
    DoctorScheduleResponse doctorScheduleToResponse(DoctorSchedule doctorSchedule);
}
