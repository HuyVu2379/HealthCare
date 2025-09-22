package fit.iuh.student.userservice.dtos.responses;


import lombok.*;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ErrorResponse<T> implements Serializable {
    private int statusCode;
    private String message;
    private boolean success = false;
    private T data;
}