package fit.iuh.student.communicationservice.dtos.requests;

import fit.iuh.student.communicationservice.enums.RoomStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRoomRequest {
    private String room_name;
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private RoomStatus status;
}
