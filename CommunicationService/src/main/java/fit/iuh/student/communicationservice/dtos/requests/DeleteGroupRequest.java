package fit.iuh.student.communicationservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteGroupRequest {
    private String groupId;
    private String userId; // ID của người yêu cầu xóa group (để kiểm tra quyền)
}
