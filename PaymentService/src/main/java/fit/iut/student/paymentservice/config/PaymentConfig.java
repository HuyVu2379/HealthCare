package fit.iut.student.paymentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "payment")
@Data
public class PaymentConfig {
    private Integer expiryMinutes;
    private String webhookUrl;
    private String returnUrl;
    private String cancelUrl;
}
