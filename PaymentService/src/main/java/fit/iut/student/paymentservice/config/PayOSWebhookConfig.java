package fit.iut.student.paymentservice.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import vn.payos.PayOS;
import vn.payos.model.webhooks.ConfirmWebhookResponse;

@Configuration
@Slf4j
public class PayOSWebhookConfig {

    private final PayOS payOS;
    private final PaymentConfig paymentConfig;

    @Autowired
    public PayOSWebhookConfig(PayOS payOS, PaymentConfig paymentConfig) {
        this.payOS = payOS;
        this.paymentConfig = paymentConfig;
    }

    @PostConstruct
    public void registerWebhook() {
        try {
            String webhookUrl = paymentConfig.getWebhookUrl();
            log.info("[PayOS Webhook Config] Registering webhook URL: {}", webhookUrl);

            ConfirmWebhookResponse response = payOS.webhooks().confirm(webhookUrl);

            if (response != null) {
                log.info("[PayOS Webhook Config] ✓ Webhook registered successfully for account: {} ({})",
                        response.getAccountName(), response.getAccountNumber());
            } else {
                log.warn("[PayOS Webhook Config] ⚠ Webhook registration returned null response");
            }
        } catch (Exception e) {
            log.error("[PayOS Webhook Config] ✗ Failed to register webhook URL: {}", e.getMessage(), e);
            // Don't throw exception to prevent application startup failure
            // Webhook can be configured manually on PayOS dashboard if needed
        }
    }
}
