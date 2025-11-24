package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment,String> {
    @Query(value = "{ 'target_type': 'DOCTOR', 'target_id': ?0 }", sort = "{ 'createdAt': -1 }")
    Page<Comment> findAllByOrderByCreatedAtDescForDoctor(Pageable pageable, String targetId);

    @Query(value = "{ 'target_id': ?0 }")
    List<Comment> findCommentsByTargetId(String targetId);

    @Query(value = "{ 'target_id': ?0 }")
    Page<Comment> findCommentsByTargetIdPagination(String targetId, Pageable pageable);
}
