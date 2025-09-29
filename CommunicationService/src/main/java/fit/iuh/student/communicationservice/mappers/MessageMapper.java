package fit.iuh.student.communicationservice.mappers;

import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.entities.Message;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    @Mapping(source = "message_id", target = "messageId")
    @Mapping(source = "group_id", target = "groupId")
    @Mapping(source = "sender_id", target = "senderId")
    @Mapping(source = "receiver_id", target = "receiverId")
    // tempMessageId được set ở service khi cần, bỏ qua ở mapper
    @Mapping(target = "tempMessageId", ignore = true)
    MessageResponse toMessageResponse(Message message);
}
