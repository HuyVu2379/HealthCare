package fit.iuh.student.userservice.dtos.responses;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SuccessEntityResponse {
    public static <T> ResponseEntity<MessageResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new MessageResponse<>(HttpStatus.OK.value(), message, true, data));
    }
    public static <T> ResponseEntity<MessageResponse<T>> found(String message, T data) {
        return new ResponseEntity<>(new MessageResponse<>(HttpStatus.FOUND.value(), message, true, data), HttpStatus.FOUND);
    }
    public static <T> ResponseEntity<MessageResponse<T>> created(String message, T data) {
        return new ResponseEntity<>(new MessageResponse<>(HttpStatus.CREATED.value(), message, true, data), HttpStatus.CREATED);
    }
}