package fit.iuh.student.communicationservice.dtos.responses;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class GroupResponse {
    private String groupId;
    private String groupName;
    private String appointmentId;
    private String lastMessageContent;
    private LocalDateTime timeLastMessage;
    private List<CreateGroupRequest.MemberDTO> members;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
