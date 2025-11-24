package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.clients.UserClient;
import fit.iuh.student.communicationservice.dtos.requests.CreateCommentRequest;
import fit.iuh.student.communicationservice.entities.Comment;
import fit.iuh.student.communicationservice.enums.CommentTargetType;
import fit.iuh.student.communicationservice.repositories.CommentRepository;
import fit.iuh.student.communicationservice.services.CommentService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserClient userClient;
    @Override
    public Comment createComment(CreateCommentRequest request) {
        try{
            Comment comment = Comment.builder()
                    .comment_id(UUID.randomUUID().toString())
                    .target_id(request.getTarget_id())
                    .target_type(request.getTarget_type())
                    .author_id(request.getAuthor_id())
                    .author_name(request.getAuthor_name())
                    .author_avatar(request.getAuthor_avatar())
                    .content(request.getContent())
                    .rating(request.getRating())
                    .imageUrls(request.getImageUrls().isEmpty() ? null : request.getImageUrls())
                    .createdAt(LocalDateTime.now())
                    .build();
            Comment cmt =  commentRepository.save(comment);
            if(cmt.getTarget_type().equals(CommentTargetType.DOCTOR)){
                List<Comment> comments = commentRepository.findCommentsByTargetId(cmt.getTarget_id());
                comments.add(cmt);
                double avgRating = comments.stream()
                        .filter(c -> c.getRating() != 0)
                        .mapToDouble(Comment::getRating)
                        .average()
                        .orElse(0.0);
                userClient.updateDoctorRating(cmt.getTarget_id(), avgRating);
            }
            return cmt;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Page<Comment> findAllByOrderByCreatedAtDescForDoctor(String doctorId,int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return commentRepository.findAllByOrderByCreatedAtDescForDoctor(pageable,doctorId);
    }

    @Override
    public List<Comment> findCommentByTargetId(String targetId, Pageable pageable) {
        return commentRepository.findCommentsByTargetIdPagination(targetId, pageable).getContent();
    }
}
