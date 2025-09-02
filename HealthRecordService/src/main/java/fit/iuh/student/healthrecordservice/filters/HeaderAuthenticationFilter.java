package fit.iuh.student.healthrecordservice.filters;

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
        log.debug("Received headers - X-Auth-User: {}, X-Auth-Role: {}", username, role);

        // Nếu không có username hoặc role, chuyển tiếp request mà không xác thực
        if (username == null || role == null) {
            log.debug("No authentication headers found, proceeding with unauthenticated request");
            filterChain.doFilter(request, response);
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

            log.debug("Successfully authenticated user: {}", username);
        } catch (Exception e) {
            log.error("Failed to set authentication from headers", e);
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }
}