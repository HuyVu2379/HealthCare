package fit.iuh.student.communicationservice.dtos.responses;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String messageId;
    private String groupId;
    private String senderId;
    private String receiverId;
    private String content;
    private LocalDateTime sendAt;
    private LocalDateTime createdAt;
}
