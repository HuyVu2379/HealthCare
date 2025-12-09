package fit.iut.student.paymentservice.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Lấy authentication headers từ Gateway
        String userEmail = request.getHeader("X-Auth-User");
        String userRole = request.getHeader("X-Auth-Role");
        String userId = request.getHeader("X-Auth-UserId");

        // Nếu có authentication headers, set vào Spring Security context
        if (userEmail != null && userRole != null && userId != null) {
            log.debug("Authentication headers found - User: {}, Role: {}, UserId: {}",
                userEmail, userRole, userId);

            // Tạo authentication token với role
            SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + userRole);
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userEmail,
                    null,
                    Collections.singletonList(authority)
                );

            // Set vào Security Context
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Security context set for user: {}", userEmail);
        } else {
            log.debug("No authentication headers found in request");
        }

        // Tiếp tục filter chain
        filterChain.doFilter(request, response);
    }
}
