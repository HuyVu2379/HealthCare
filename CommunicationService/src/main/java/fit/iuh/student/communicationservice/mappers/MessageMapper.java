package fit.iuh.student.communicationservice.mappers;

import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.entities.Message;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageResponse toMessageResponse(Message message);
}
