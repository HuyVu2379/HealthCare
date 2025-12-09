package fit.iuh.student.userservice.dtos.admin;

import fit.iuh.student.userservice.enums.Status;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private Status newStatus;
    private String reason;
}
