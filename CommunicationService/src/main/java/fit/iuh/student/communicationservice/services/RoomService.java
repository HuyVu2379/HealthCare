package fit.iuh.student.communicationservice.services;

import fit.iuh.student.communicationservice.dtos.requests.CreateRoomRequest;
import fit.iuh.student.communicationservice.entities.Room;
import fit.iuh.student.communicationservice.enums.RoomStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface RoomService {
    Room createRoomIfNotExists(CreateRoomRequest request);
    List<Room> getRoomByDate(LocalDateTime date);
    Room updateRoomStatus(String roomId, RoomStatus status);
}
