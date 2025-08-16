package fit.iuh.student.userservice.publisher;

import fit.iuh.student.userservice.publisher.events.UserEvent;
import fit.iuh.student.userservice.publisher.payload.UserEventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {
    private final RabbitTemplate rabbitTemplate;
    private static final String USER_NOTIFICATION_QUEUE = "USER_NOTIFICATION_QUEUE";

    /**
     * Publishes an OTP registration event to the notification service
     * @param email The recipient's email address
     * @param subject The email subject
     */
    public void publishOtpRegistrationEvent(String email, String subject) {
        try {
            UserEventPayload payload = UserEventPayload.builder()
                    .eventType(UserEvent.OTP_REGISTER)
                    .email(email)
                    .subject(subject)
                    .build();
            
            rabbitTemplate.convertAndSend(USER_NOTIFICATION_QUEUE, payload);
            log.info("Published OTP registration event for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to publish OTP registration event for email: {}", email, e);
        }
    }

    /**
     * Publishes an OTP password reset event to the notification service
     * @param email The recipient's email address
     * @param subject The email subject
     */
    public void publishOtpResetPasswordEvent(String email, String subject) {
        try {
            UserEventPayload payload = UserEventPayload.builder()
                    .eventType(UserEvent.OTP_RESET_PASSWORD)
                    .email(email)
                    .subject(subject)
                    .build();
            
            rabbitTemplate.convertAndSend(USER_NOTIFICATION_QUEUE, payload);
            log.info("Published OTP reset password event for email: {}", email);
        } catch (Exception e) {
            log.error("Failed to publish OTP reset password event for email: {}", email, e);
        }
    }
}
