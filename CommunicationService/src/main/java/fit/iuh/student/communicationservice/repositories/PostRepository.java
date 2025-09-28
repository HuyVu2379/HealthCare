package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends MongoRepository<Post,String> {
    @Query("{'author_id': ?0}")
    List<Post> findByAuthorId(String userId);

    @Query("{}")
    Page<Post> findAllBy(Pageable pageable);
}
