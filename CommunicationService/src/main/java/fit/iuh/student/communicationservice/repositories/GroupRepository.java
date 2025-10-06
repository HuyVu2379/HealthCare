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

    @Query("{ 'members.userId': { $all: ?0 } }")
    Optional<Group> findGroupByMemberIds(List<String> memberIds);

    Group findByGroupId(String groupId);

    void deleteByGroupId(String groupId);
}
