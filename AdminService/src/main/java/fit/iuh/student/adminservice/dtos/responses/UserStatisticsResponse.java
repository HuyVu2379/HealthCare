package fit.iuh.student.adminservice.dtos.responses;

import fit.iuh.student.adminservice.enums.Role;
import fit.iuh.student.adminservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private Map<Role, Long> usersByRole;
    private Map<Status, Long> usersByStatus;
    private Long totalUsers;
    private Long activeUsers;
    private Long blockedUsers;
}
