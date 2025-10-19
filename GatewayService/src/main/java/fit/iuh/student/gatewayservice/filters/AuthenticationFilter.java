package fit.iuh.student.gatewayservice.filters;

import fit.iuh.student.gatewayservice.exceptions.errors.UnauthorizedException;
import fit.iuh.student.gatewayservice.utils.JWTUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.PathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private final JWTUtil jwtUtil;
    private final PathMatcher pathMatcher;
    private final List<String> publicEndpoints = Arrays.asList(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/refresh-token",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/register-admin",
            "/api/v1/auth/send-otp-reset-password/**",
            "/api/v1/auth/validate-otp",
            "/api/v1/auth/reset-password",
            "/api/v1/auth/google",
            "/ws/**",
            "/api/v1/health-metrics/get-health-metrics-latest/**",
            "/api/v1/health-metrics/get-health-metrics/**",
            "/api/v1/health-metrics/get-health-metrics-with-batch/**",
            "/api/v1/predicts/get-predict-history/**");
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    public AuthenticationFilter(JWTUtil jwtUtil, PathMatcher pathMatcher) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
        this.pathMatcher = pathMatcher;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String path = exchange.getRequest().getPath().toString();
            logger.info("Request path: {}", path);

            if (isPublicEndpoint(path)) {
                return chain.filter(exchange);
            }

            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            logger.info("Authorization header: {}", authHeader);
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return Mono.error(new UnauthorizedException("Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7).trim();
            logger.info("Extracted JWT token: {}", token);

            return jwtUtil.validateToken(token)
                    .flatMap(isValid -> {
                        if (isValid) {
                            String email = jwtUtil.extractEmail(token);
                            String role = jwtUtil.extractRole(token);
                            String userId = jwtUtil.extractUserId(token);
                            ServerWebExchange modifiedExchange = exchange.mutate()
                                    .request(exchange.getRequest().mutate()
                                            .header("X-Auth-User", email)
                                            .header("X-Auth-Role", role)
                                            .header("X-Auth-UserId", userId)
                                            .header("X-Auth-Token", token)
                                            .build())
                                    .build();
                            return chain.filter(modifiedExchange);
                        } else {
                            return Mono.error(new UnauthorizedException("Invalid token"));
                        }
                    })
                    .onErrorResume(e -> Mono.error(new UnauthorizedException("Token validation failed: " + e.getMessage())));
        };
    }

    private boolean isPublicEndpoint(String path) {
        return publicEndpoints.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    public static class Config {
    }
}
