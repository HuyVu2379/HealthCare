package fit.iuh.student.adminservice.configs;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign configuration for AdminService
 * Uses static authentication headers for service-to-service calls
 * Similar to PaymentService's FeignAuthConfig
 */
@Slf4j
@Configuration
public class FeignConfig {

    @Value("${admin.service.auth.user:admin-service}")
    private String authUser;

    @Value("${admin.service.auth.role:ADMIN}")
    private String authRole;

    @Value("${admin.service.auth.user-id:admin-service-system}")
    private String authUserId;

    @Value("${admin.service.auth.token:internal-admin-service-token}")
    private String authToken;

    /**
     * RequestInterceptor to add static authentication headers to all Feign requests
     * These headers are used for service-to-service authentication
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {
            template.header("X-Auth-User", authUser);
            template.header("X-Auth-Role", authRole);
            template.header("X-Auth-UserId", authUserId);
            template.header("X-Auth-Token", authToken);

            log.debug("Adding service-to-service auth headers - User: {}, Role: {}", authUser, authRole);
        };
    }
}
