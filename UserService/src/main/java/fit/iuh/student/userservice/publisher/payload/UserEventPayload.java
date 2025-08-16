package fit.iuh.student.userservice.publisher.payload;

import fit.iuh.student.userservice.publisher.events.UserEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventPayload {
    private String receiptId;
    private UserEvent eventType;
    private String email;
    private String subject;
    private String otp;
}