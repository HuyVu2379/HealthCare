package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Summary;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SummaryRepository extends MongoRepository<Summary,String> {
    Summary findByGroupId(String groupId);
}
