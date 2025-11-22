package fit.iuh.student.communicationservice.controllers;

import fit.iuh.student.communicationservice.dtos.requests.CreatePostRequest;
import fit.iuh.student.communicationservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.communicationservice.dtos.responses.SuccessResponse;
import fit.iuh.student.communicationservice.entities.Post;
import fit.iuh.student.communicationservice.services.PostService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
@AllArgsConstructor
public class PostController {
    private final PostService postService;
    @PreAuthorize("hasAnyRole('DOCTOR')")
    @PostMapping("/create")
    public ResponseEntity<SuccessResponse.MessageResponse<Post>> createPost(
            @RequestBody CreatePostRequest request
    ) {
        return SuccessEntityResponse.ok("Create post successfully", postService.createPost(request));
    }

    @GetMapping("/getPostWithPagination")
    public ResponseEntity<SuccessResponse.MessageResponse<List<Post>>> getPostWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        return SuccessEntityResponse.ok("Get posts with pagination successfully",
                postService.findPostsWithPagination(page, size, sortBy, sortDir));
    }
}
