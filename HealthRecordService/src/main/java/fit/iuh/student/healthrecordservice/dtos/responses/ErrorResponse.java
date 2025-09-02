package fit.iuh.student.healthrecordservice.dtos.responses;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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