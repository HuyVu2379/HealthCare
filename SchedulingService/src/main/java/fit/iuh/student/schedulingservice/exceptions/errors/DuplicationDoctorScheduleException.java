package fit.iuh.student.schedulingservice.exceptions.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicationDoctorScheduleException extends RuntimeException {
    public DuplicationDoctorScheduleException(String message) {
        super(message);
    }
}
