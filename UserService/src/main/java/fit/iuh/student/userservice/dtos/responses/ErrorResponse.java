package fit.iuh.student.userservice.dtos.responses;


import lombok.*;

import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ErrorResponse {
    private int status;
    private String message;
    private boolean success;
    private Map<String, String> details;
    private long timestamp;
    private String path;
}