package fit.iut.student.paymentservice.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Adds authentication headers to Feign calls using values from application configuration.
 */
@Configuration
public class FeignAuthConfig {

    @Value("${scheduling.service.auth.user:payment-service}")
    private String authUser;

    @Value("${scheduling.service.auth.role:SYSTEM}")
    private String authRole;

    @Value("${scheduling.service.auth.user-id:payment-service}")
    private String authUserId;

    @Value("${scheduling.service.auth.token:internal-service-token}")
    private String authToken;

    @Bean
    public RequestInterceptor schedulingAuthInterceptor() {
        return template -> {
            template.header("X-Auth-User", authUser);
            template.header("X-Auth-Role", authRole);
            template.header("X-Auth-UserId", authUserId);
            template.header("X-Auth-Token", authToken);
        };
    }
}
