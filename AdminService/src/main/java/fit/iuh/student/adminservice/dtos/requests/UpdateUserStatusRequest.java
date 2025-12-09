package fit.iuh.student.adminservice.dtos.requests;

import fit.iuh.student.adminservice.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserStatusRequest {
    private Status newStatus; // ACTIVE, INACTIVE, BLOCKED
    private String reason; // Optional: reason for status change
}
