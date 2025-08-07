package fit.iuh.student.gatewayservice.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.security.Key;

@Component
public class JWTUtil {

    private static final Logger logger = LoggerFactory.getLogger(JWTUtil.class);

    @Value("${jwt.secretKey}")
    private String secretKey;

    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractEmail(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            logger.error("Error extracting email from token: {}", e.getMessage());
            throw e;
        }
    }
    public String extractUserId(String token){
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", String.class);
        }catch (Exception e){
            logger.error("Error extracting user id from token: {}", e.getMessage());
            throw e;
        }
    }
    public String extractRole(String token){
        try{
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("role", String.class);
        }catch (Exception e){
            logger.error("Error extracting role from token: {}", e.getMessage());
            throw e;
        }
    }
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromCallable(() -> {
            try {
                logger.info("Validating JWT token: {}", token);
                Jwts.parserBuilder()
                        .setSigningKey(getSignInKey())
                        .build()
                        .parseClaimsJws(token);
                logger.info("Token validated successfully");
                return true;
            } catch (SignatureException e) {
                logger.error("Invalid JWT signature: {}", e.getMessage());
                return false;
            } catch (ExpiredJwtException e) {
                logger.error("JWT token is expired: {}", e.getMessage());
                return false;
            } catch (MalformedJwtException e) {
                logger.error("Invalid JWT token format: {}", e.getMessage());
                return false;
            } catch (IllegalArgumentException e) {
                logger.error("Illegal argument in token: {}", e.getMessage());
                return false;
            } catch (Exception e) {
                logger.error("JWT validation error: {}", e.getMessage(), e);
                return false;
            }
        });
    }
}
