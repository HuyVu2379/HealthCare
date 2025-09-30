package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Group;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends MongoRepository<Group,String> {
    @Query("{ 'members.userId': ?0 }")
    List<Group> findByMembersUserId(String userId);
    boolean existsGroupByGroupName(String groupName);

    // Tìm group 1-1 chỉ theo 2 user (không phụ thuộc thứ tự), bất kể appointment_id
    @Query("{ 'members.userId': { $all: [?0, ?1] }, 'members': { $size: 2 } }")
    Optional<Group> findOneToOneGroupByMembers(String userId1, String userId2);
}
