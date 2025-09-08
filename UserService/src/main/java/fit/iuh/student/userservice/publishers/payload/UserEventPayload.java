package fit.iuh.student.userservice.publishers.payload;

import fit.iuh.student.userservice.publishers.events.UserEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEventPayload {
    public UserEventPayload(String email, String subject) {
        this.email = email;
        this.subject = subject;
    }
    private String receiptId;
    private UserEvent eventType;
    private String email;
    private String subject;
    private String otp;
}