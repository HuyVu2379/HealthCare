package fit.iuh.student.communicationservice.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotBlank(message = "Group ID không được để trống")
    private String groupId;

    @NotBlank(message = "Sender ID không được để trống")
    private String senderId;

    @NotBlank(message = "Content không được để trống")
    private String content;

    private String messageType = "TEXT"; // TEXT, IMAGE, FILE, etc.

    // Frontend gửi tạm để match optimistic UI
    private String tempMessageId;
}
