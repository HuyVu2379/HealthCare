package fit.iuh.student.communicationservice.services;

import fit.iuh.student.communicationservice.dtos.requests.CreatePostRequest;
import fit.iuh.student.communicationservice.entities.Post;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PostService {
    Post createPost(CreatePostRequest request);
    Post findPostById(String postId);
    List<Post> findPostsByUserId(String userId);
    Page<Post> findPostsWithPagination(int page, int size, String sortBy, String sortDir);
}
