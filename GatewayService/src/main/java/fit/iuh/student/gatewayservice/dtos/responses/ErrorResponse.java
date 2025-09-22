package fit.iuh.student.gatewayservice.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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