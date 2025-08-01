package fit.iuh.student.communicationservice.entities;

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
@EqualsAndHashCode(of = {"commentId"})
public class Comment {
    @Id
    private String comment_id;
    private String post_id;
    private String author_id; // Người viết bình luận
    private String content;
    private List<String> imageUrls;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
