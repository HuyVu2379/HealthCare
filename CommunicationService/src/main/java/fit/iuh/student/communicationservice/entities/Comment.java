package fit.iuh.student.communicationservice.entities;

import fit.iuh.student.communicationservice.enums.CommentTargetType;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@EqualsAndHashCode(of = {"comment_id"})
@Builder
public class Comment {
    @Id
    @Field("comment_id")
    private String comment_id;
    @Field("target_id")
    private String target_id;
    @Field("target_type")
    private CommentTargetType target_type;
    @Field("author_id")
    private String author_id; // Người viết bình luận
    @Field("author_name")
    private String author_name; // Tên người viết bình luận
    @Field("author_avatar")
    private String author_avatar; // Ảnh đại diện của người viết bình luận
    @Field("content")
    private String content;
    @Field("rating")
    private int rating; // đánh giá dành cho bác sĩ, nếu có 1-5 sao
    @Field("imageUrls")
    private List<String> imageUrls;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
