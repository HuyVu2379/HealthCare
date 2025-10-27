package fit.iuh.student.communicationservice.repositories;

import fit.iuh.student.communicationservice.entities.Room;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RoomRepository extends MongoRepository<Room,String> {
    boolean existsByAppointmentId(String appointmentId);

    List<Room> findAllByCreatedAtBetween(LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
