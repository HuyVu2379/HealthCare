package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.CreatePostRequest;
import fit.iuh.student.communicationservice.entities.Post;
import fit.iuh.student.communicationservice.exceptions.errors.NotFoundException;
import fit.iuh.student.communicationservice.repositories.PostRepository;
import fit.iuh.student.communicationservice.services.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {
    private final PostRepository postRepository;

    @Override
    public Post createPost(CreatePostRequest request) {
        try{
            Post post = Post.builder()
                    .author_id(request.getAuthor_id())
                    .title(request.getTitle())
                    .content(request.getContent())
                    .author_name(request.getAuthor_name())
                    .author_avatar(request.getAuthor_avatar())
                    .image_urls(request.getImage_urls())
                    .category(request.getCategory())
                    .build();
            return postRepository.save(post);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Post findPostById(String postId) {
        try{
            return postRepository.findById(postId).orElseThrow(()->new NotFoundException("Post not found with id: " + postId));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<Post> findPostsByUserId(String userId) {
        try{
            return postRepository.findByAuthorId(userId);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<Post> findPostsWithPagination(int page, int size, String sortBy, String sortDir) {
       try{
           if(sortBy == null || sortBy.isEmpty()){
               sortBy = "createdAt";
           }
           Sort.Direction direction = Sort.Direction.ASC;
           if(sortDir != null && sortDir.equalsIgnoreCase("DESC")){
               direction = Sort.Direction.DESC;
           }
           Sort sort = Sort.by(direction, sortBy);
           Pageable pageable = PageRequest.of(page, size, sort);
           return postRepository.findAllBy(pageable).getContent();
       } catch (Exception e) {
           throw e;
       }
    }
}
