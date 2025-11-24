package fit.iuh.student.communicationservice.services;

import fit.iuh.student.communicationservice.dtos.requests.CreateCommentRequest;
import fit.iuh.student.communicationservice.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CommentService {
    Comment createComment(CreateCommentRequest request);
    Page<Comment> findAllByOrderByCreatedAtDescForDoctor(String doctorId,int page, int size);
    List<Comment> findCommentByTargetId(String targetId, Pageable pageable);
}
