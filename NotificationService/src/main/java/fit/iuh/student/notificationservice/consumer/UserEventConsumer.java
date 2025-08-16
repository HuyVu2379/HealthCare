package fit.iuh.student.notificationservice.consumer;

import fit.iuh.student.notificationservice.consumer.payload.UserEventPayload;
import fit.iuh.student.notificationservice.services.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class UserEventConsumer {
    private final EmailService emailService;
    @RabbitListener(queues = "USER_NOTIFICATION_QUEUE")
    public void handleUserEvent(UserEventPayload payload){
        try{
            log.info("Received user event: {}", payload.getEventType());
            
            switch (payload.getEventType()){
                case OTP_REGISTER:
                    emailService.sendOtpRegisterEmail(payload);
                    break;
                case OTP_RESET_PASSWORD:
                    emailService.sendOtpResetPasswordEmail(payload);
                    break;
                default:
                    log.warn("Unknown event type: {}", payload.getEventType());
            }
        }catch (Exception e){
            log.error("Error handling user event: {}", e.getMessage(), e);
            throw e;
        }
    }
}
