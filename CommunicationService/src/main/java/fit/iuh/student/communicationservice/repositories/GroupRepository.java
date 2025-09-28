package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface GroupRepository extends MongoRepository<Group,String> {
    @Query("{ 'members.userId': ?0 }")
    List<Group> findByMembersUserId(String userId);
}
