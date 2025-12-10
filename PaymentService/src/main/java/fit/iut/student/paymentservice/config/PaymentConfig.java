package fit.iut.student.paymentservice.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentConfig {
    @Value("${payment.expiry-minutes}")
    private Integer expiryMinutes;
    @Value("${payment.webhook-url}")
    private String webhookUrl;
    @Value("${payment.webhook-auto-register:false}")
    private boolean webhookAutoRegister;
    @Value("${payment.return-url}")
    private String returnUrl;
    @Value("${payment.cancel-url}")
    private String cancelUrl;
}
