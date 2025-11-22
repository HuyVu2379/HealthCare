package fit.iuh.student.communicationservice.controllers;

import fit.iuh.student.communicationservice.dtos.requests.CreateCommentRequest;
import fit.iuh.student.communicationservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.communicationservice.dtos.responses.SuccessResponse;
import fit.iuh.student.communicationservice.entities.Comment;
import fit.iuh.student.communicationservice.services.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/create")
    public ResponseEntity<SuccessResponse.MessageResponse<Comment>> createComment(
            @RequestBody CreateCommentRequest request
    ) {
        Comment comment = commentService.createComment(request);
        return SuccessEntityResponse.created("Comment created successfully", comment);
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<SuccessResponse.MessageResponse<Page<Comment>>> getCommentByDoctorId(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return SuccessEntityResponse.ok("Comments fetched successfully",
                commentService.findAllByOrderByCreatedAtDescForDoctor(doctorId,page,size));
    }

    @GetMapping("/byPost/{postId}")
    public ResponseEntity<SuccessResponse.MessageResponse<List<Comment>>> getCommentsByPostId(
    @PathVariable String postId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size){
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page,size,sort);
        return SuccessEntityResponse.ok("Comments fetched successfully",
                commentService.findCommentByTargetId(postId,pageable));
    }

}
