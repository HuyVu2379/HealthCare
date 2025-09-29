package fit.iuh.student.communicationservice.entities;

import fit.iuh.student.communicationservice.enums.Category;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@Builder
@EqualsAndHashCode(of = {"postId","authorId"})
public class Post {
    @Id
    private String post_id;
    private String author_id;
    private String title;
    private String content;
    private List<String> image_urls;
    private Category category;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
