package fit.iuh.student.communicationservice.dtos.responses;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GroupResponse {
    private String groupId;
    private String groupName;
    private String appointmentId;
    private List<String> memberIds;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
