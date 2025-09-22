package fit.iuh.student.communicationservice.services;

import fit.iuh.student.communicationservice.dtos.requests.SendMessageRequest;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;

import java.util.List;

public interface MessageService {
    MessageResponse sendMessage(SendMessageRequest request);
    List<MessageResponse> getMessagesByGroupId(String groupId);
    List<MessageResponse> getMessagesByGroupIdWithPagination(String groupId, Integer page, Integer size);
}
