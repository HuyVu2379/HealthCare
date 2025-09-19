package fit.iuh.student.gatewayservice.dtos.responses;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ErrorEntityResponse {
    public static <T> ResponseEntity<ErrorResponse<T>> error(String message, int status, T data) {
        return new ResponseEntity<>(new ErrorResponse<>(status, message, false, data), HttpStatus.valueOf(status));
    }
}
