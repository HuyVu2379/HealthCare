package fit.iuh.student.communicationservice.dtos.requests;

import fit.iuh.student.communicationservice.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    private String post_id;
    private String author_id;
    private String title;
    private String content;
    private Category category;
}
