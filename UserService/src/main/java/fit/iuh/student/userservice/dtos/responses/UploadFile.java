package fit.iuh.student.userservice.dtos.responses;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class UploadFile {
    private List<String> imageUrls;
}
