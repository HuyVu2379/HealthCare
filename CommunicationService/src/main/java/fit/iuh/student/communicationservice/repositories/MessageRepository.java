package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    @Query(value = "{ 'group_id': ?0 }", sort = "{ 'createdAt': 1 }")
    List<Message> findByGroup_idOrderByCreatedAtAsc(String groupId);
    @Query(value = "{ 'group_id': ?0 }", sort = "{ 'sendAt': -1 }")
    List<Message> findByGroup_idOrderBySendAtDesc(String groupId, Pageable pageable);
    // Lấy message mới nhất (sendAt desc, createdAt desc) với LIMIT 1 qua pageable
    @Query(value = "{ 'group_id': ?0 }", sort = "{ 'sendAt': -1, 'createdAt': -1 }")
    List<Message> findLatestMessageByGroupId(String groupId, Pageable pageable);

    @Query(value = "{ 'group_id': ?0 }", delete = true)
    void deleteAllByGroup_id(String groupId);
}
