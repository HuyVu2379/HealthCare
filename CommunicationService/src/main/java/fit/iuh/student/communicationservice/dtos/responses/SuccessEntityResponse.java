package fit.iuh.student.communicationservice.dtos.responses;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class SuccessEntityResponse {
    public static <T> ResponseEntity<SuccessResponse.MessageResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(new SuccessResponse.MessageResponse<>(HttpStatus.OK.value(), message, true, data));
    }
    public static <T> ResponseEntity<SuccessResponse.MessageResponse<T>> found(String message, T data) {
        return new ResponseEntity<>(new SuccessResponse.MessageResponse<>(HttpStatus.FOUND.value(), message, true, data), HttpStatus.FOUND);
    }
    public static <T> ResponseEntity<SuccessResponse.MessageResponse<T>> created(String message, T data) {
        return new ResponseEntity<>(new SuccessResponse.MessageResponse<>(HttpStatus.CREATED.value(), message, true, data), HttpStatus.CREATED);
    }
}