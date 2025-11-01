package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room,String> {
    boolean existsByAppointmentId(String appointmentId);
    @Query("{ $or: [ { 'doctorId': ?0 }, { 'patientId': ?0 } ], 'createdAt': { $gte: ?1, $lte: ?2 } }")
    List<Room> findAllByCreatedAtBetween(String userId,LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
