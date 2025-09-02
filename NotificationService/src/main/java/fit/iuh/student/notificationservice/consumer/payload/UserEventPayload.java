package fit.iuh.student.notificationservice.consumer.payload;

import fit.iuh.student.notificationservice.consumer.event.UserEvent;
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