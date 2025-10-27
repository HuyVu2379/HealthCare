package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.CreateRoomRequest;
import fit.iuh.student.communicationservice.entities.Room;
import fit.iuh.student.communicationservice.enums.RoomStatus;
import fit.iuh.student.communicationservice.exceptions.errors.NotFoundException;
import fit.iuh.student.communicationservice.repositories.RoomRepository;
import fit.iuh.student.communicationservice.services.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;

    @Override
    public Room createRoomIfNotExists(CreateRoomRequest request) {
        try {
            boolean existRoom = roomRepository.existsByAppointmentId(request.getAppointmentId());
            if (!existRoom) {
                return roomRepository.save(Room.builder()
                        .room_name(request.getRoom_name())
                        .appointmentId(request.getAppointmentId())
                        .doctorId(request.getDoctorId())
                        .patientId(request.getPatientId())
                        .status(request.getStatus())
                        .build());
            }
        } catch (Exception e) {
            throw e;
        }
        return null;
    }

    @Override
    public List<Room> getRoomByDate(LocalDateTime date) {
        try{
            return roomRepository.findAllByCreatedAtBetween(date.toLocalDate().atStartOfDay(), date.toLocalDate().atTime(23,59,59));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Room updateRoomStatus(String roomId, RoomStatus status) {
        try{
            Room room = roomRepository.findById(roomId).orElseThrow(() -> new NotFoundException("Room not found"));
            room.setStatus(status);
            return roomRepository.save(room);
        } catch (Exception e) {
            throw e;
        }
    }
}
