package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    @Query(value = "{ 'group.id': ?0 }", sort = "{ 'createdAt': 1 }")
    List<Message> findByGroup_idOrderByCreatedAtAsc(String groupId);
    @Query(value = "{ 'group.id': ?0 }", sort = "{ 'createdAt': -1 }")
    List<Message> findByGroup_idOrderByCreatedAtDesc(String groupId, Pageable pageable);
}
