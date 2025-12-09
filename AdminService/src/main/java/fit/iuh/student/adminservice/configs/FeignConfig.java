package fit.iuh.student.adminservice.configs;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Configuration
public class FeignConfig {

    /**
     * RequestInterceptor to forward authentication headers from incoming request to Feign clients
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

                if (attributes != null) {
                    HttpServletRequest request = attributes.getRequest();

                    // Forward authentication headers
                    String authUser = request.getHeader("X-Auth-User");
                    String authRole = request.getHeader("X-Auth-Role");
                    String authUserId = request.getHeader("X-Auth-UserId");
                    String authToken = request.getHeader("X-Auth-Token");

                    if (authUser != null) {
                        template.header("X-Auth-User", authUser);
                        log.debug("Forwarding X-Auth-User: {}", authUser);
                    }
                    if (authRole != null) {
                        template.header("X-Auth-Role", authRole);
                        log.debug("Forwarding X-Auth-Role: {}", authRole);
                    }
                    if (authUserId != null) {
                        template.header("X-Auth-UserId", authUserId);
                        log.debug("Forwarding X-Auth-UserId: {}", authUserId);
                    }
                    if (authToken != null) {
                        template.header("X-Auth-Token", authToken);
                        log.debug("Forwarding X-Auth-Token");
                    }
                } else {
                    log.warn("No request attributes available - cannot forward auth headers");
                }
            }
        };
    }
}
