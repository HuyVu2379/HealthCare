package fit.iuh.student.adminservice.filters;

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
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HeaderAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String username = request.getHeader("X-Auth-User");
        String role = request.getHeader("X-Auth-Role");
        String userId = request.getHeader("X-Auth-UserId");
        String accessToken = request.getHeader("X-Auth-Token");

        log.debug("AdminService - Received headers - X-Auth-User: {}, X-Auth-Role: {}, X-Auth-UserId: {}",
                username, role, userId);

        // If no username or role, proceed without authentication
        if (username == null || role == null) {
            log.debug("No authentication headers found, proceeding with unauthenticated request");
            filterChain.doFilter(request, response);
            return;
        }

        // AdminService requires ADMIN role
        if (!"ADMIN".equals(role) && !"ROLE_ADMIN".equals(role)) {
            log.warn("Access denied - User {} with role {} attempted to access AdminService", username, role);
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("{\"error\": \"Access denied. ADMIN role required.\"}");
            response.setContentType("application/json");
            return;
        }

        try {
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            String normalizedRole = role.startsWith("ROLE_") ? role : "ROLE_" + role;
            authorities.add(new SimpleGrantedAuthority(normalizedRole));
            authorities.add(new SimpleGrantedAuthority(role));

            log.debug("Setting up authentication with authorities: {}", authorities);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userId, null, authorities);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Successfully authenticated ADMIN user: {}", username);
        } catch (Exception e) {
            log.error("Failed to set authentication from headers", e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
