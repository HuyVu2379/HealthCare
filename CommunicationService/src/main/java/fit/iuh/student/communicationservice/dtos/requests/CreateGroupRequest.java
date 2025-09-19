package fit.iuh.student.communicationservice.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateGroupRequest {
    @NotBlank(message = "Group name is required")
    private String groupName;

    private String appointmentId;

    @NotEmpty(message = "Member IDs cannot be empty")
    private List<String> memberIds;
}
