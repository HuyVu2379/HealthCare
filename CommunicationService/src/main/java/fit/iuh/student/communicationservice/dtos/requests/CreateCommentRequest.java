package fit.iuh.student.communicationservice.dtos.requests;

import fit.iuh.student.communicationservice.enums.CommentTargetType;
import lombok.Data;

import java.util.List;

@Data
public class CreateCommentRequest {
    private String target_id;
    private CommentTargetType target_type;
    private String author_id; // Người viết bình luận
    private String author_name; // Tên người viết bình luận
    private String author_avatar; // Ảnh đại diện của người viết bình luận
    private String content;
    private int rating; // đánh giá dành cho bác sĩ, nếu có 1-5 sao
    private List<String> imageUrls;
}
