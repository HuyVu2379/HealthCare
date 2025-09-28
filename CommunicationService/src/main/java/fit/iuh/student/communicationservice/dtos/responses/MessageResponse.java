package fit.iuh.student.communicationservice.dtos.responses;

import lombok.Data;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
public class MessageResponse {
    private String messageId;
    private String groupId;
    private String senderId;
    private String receiverId;
    private String content;
    private Timestamp sendAt;
    private LocalDateTime createdAt;
}
