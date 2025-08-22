package fit.iuh.student.communicationservice.entities;

import fit.iuh.student.communicationservice.enums.CommentTargetType;
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
    private String target_id;
    private CommentTargetType target_type;
    private String author_id; // Người viết bình luận
    private String author_name; // Tên người viết bình luận
    private String author_avatar; // Ảnh đại diện của người viết bình luận
    private String content;
    private int rating; // đánh giá dành cho bác sĩ, nếu có 1-5 sao
    private List<String> imageUrls;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
