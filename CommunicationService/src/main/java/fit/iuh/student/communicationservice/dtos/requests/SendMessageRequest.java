package fit.iuh.student.communicationservice.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SendMessageRequest {
    @NotBlank(message = "Group ID is required")
    private String groupId;

    @NotBlank(message = "Sender ID is required")
    private String senderId;

    private String receiverId;

    @NotBlank(message = "Content is required")
    private String content;
}
