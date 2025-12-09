package fit.iuh.student.userservice.dtos.admin;

import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import lombok.*;

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
    private Long inactiveUsers;
    private Long blockedUsers;
}
