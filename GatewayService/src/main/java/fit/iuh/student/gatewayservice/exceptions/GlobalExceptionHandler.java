package fit.iuh.student.gatewayservice.exceptions;

import fit.iuh.student.gatewayservice.dtos.responses.ErrorEntityResponse;
import fit.iuh.student.gatewayservice.dtos.responses.ErrorResponse;
import fit.iuh.student.gatewayservice.exceptions.errors.UnauthorizedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(UnauthorizedException.class)
    private ResponseEntity<ErrorResponse<String>> unauthorizedResponse(ServerWebExchange exchange, String message) {
        return ErrorEntityResponse.error(message, HttpStatus.UNAUTHORIZED.value(), exchange.getRequest().getURI().getPath());
    }
}
