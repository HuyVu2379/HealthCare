package fit.iuh.student.communicationservice.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GetMessagesRequest {
    @NotBlank(message = "Group ID is required")
    private String groupId;

    private Integer page = 0;
    private Integer size = 20;
}
