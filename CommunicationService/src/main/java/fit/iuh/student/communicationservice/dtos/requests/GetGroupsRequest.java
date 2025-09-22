package fit.iuh.student.communicationservice.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
@Data
public class GetGroupsRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    private Integer page = 0;
    private Integer size = 20;
}
