package fit.iuh.student.communicationservice.mappers;

import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.entities.Group;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GroupMapper {
    GroupResponse toGroupResponse(Group group);
}
