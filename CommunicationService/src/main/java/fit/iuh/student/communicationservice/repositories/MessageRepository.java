package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends MongoRepository<Message,String> {
    List<Message> findByGroup_idOrderByCreatedAtAsc(String groupId);
    List<Message> findByGroup_idOrderByCreatedAtDesc(String groupId, Pageable pageable);
}
