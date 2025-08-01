package fit.iuh.student.userservice.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ErrorResponse {
    private int status;
    private String message;
    private boolean success;
    private Map<String, String> details;
    private long timestamp;
    private String path;
}