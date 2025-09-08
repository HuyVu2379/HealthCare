package fit.iuh.student.healthrecordservice.exceptions.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicationObjectException extends RuntimeException {
    public DuplicationObjectException(String message) {
        super(message);
    }
}
