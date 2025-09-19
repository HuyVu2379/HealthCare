package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group,String> {
    List<Group> findByMemberIdsContaining(String userId);
}
