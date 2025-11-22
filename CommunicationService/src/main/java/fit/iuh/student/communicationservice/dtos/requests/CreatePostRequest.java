package fit.iuh.student.communicationservice.dtos.requests;

import fit.iuh.student.communicationservice.enums.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {
    private String author_id;
    private String author_name;
    private String author_avatar;
    private List<String> image_urls;
    private String title;
    private String content;
    private Category category;
}
