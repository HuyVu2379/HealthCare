package fit.iuh.student.communicationservice.entities;

import fit.iuh.student.communicationservice.enums.RoomStatus;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document
@Data
@Builder
public class Room {
    @Id
    private String room_id;
    private String room_name;
    private String appointmentId;
    private String doctorId;
    private String patientId;
    private RoomStatus status;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
