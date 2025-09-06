package fit.iuh.student.userservice.publishers;

import fit.iuh.student.userservice.publishers.payload.UserEventPayload;
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
     * @param payload
     */
    public void publishOtpRegistrationEvent(UserEventPayload payload) {
        try {
            rabbitTemplate.convertAndSend(USER_NOTIFICATION_QUEUE, payload);
            log.info("Published OTP registration event for email: {}", payload.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish OTP registration event for email: {}", payload.getEmail(), e);
        }
    }

    /**
     * Publishes an OTP password reset event to the notification service
     * @param payload
     */
    public void publishOtpResetPasswordEvent(UserEventPayload payload) {
        try {
            rabbitTemplate.convertAndSend(USER_NOTIFICATION_QUEUE, payload);
            log.info("Published OTP reset password event for email: {}", payload.getEmail());
        } catch (Exception e) {
            log.error("Failed to publish OTP reset password event for email: {}", payload.getEmail(), e);
        }
    }
}
