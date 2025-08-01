package fit.iuh.student.schedulingservice.dtos.responses;


import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class MessageResponse<T> implements Serializable {
    private int statusCode;
    private String message;
    private boolean success = false;
    private T data;

    public MessageResponse(int statusCode, String message, boolean success) {
        this.statusCode = statusCode;
        this.message = message;
        this.success = success;
        this.data = null;
    }
    public MessageResponse(int statusCode, String message, boolean success, T data) {
        this.statusCode = statusCode;
        this.message = message;
        this.success = success;
        this.data = data;
    }
}