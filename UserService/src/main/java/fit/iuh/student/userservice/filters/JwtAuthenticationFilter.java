package fit.iuh.student.userservice.filters;

import fit.iuh.student.userservice.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for JWT authentication
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Value("${jwt.header}")
    private String authHeader;

    @Value("${jwt.prefix}")
    private String authPrefix;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader(this.authHeader);
        final String jwt;
        final String userEmail;

        // If no auth header or doesn't start with Bearer, continue to next filter
        if (authHeader == null || !authHeader.startsWith(authPrefix + " ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extract JWT token from auth header
        jwt = authHeader.substring(authPrefix.length() + 1);
        
        try {
            // Extract user email from JWT token
            userEmail = jwtService.extractUsername(jwt);
            
            // If user email is not null and no authentication exists in security context
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Load user details from database
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
                
                // Validate token
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    // Create authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    // Set authentication details
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    
                    // Set authentication in security context
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log exception (in a real application)
            // logger.error("JWT Authentication failed", e);
        }
        
        // Continue to next filter
        filterChain.doFilter(request, response);
    }
}