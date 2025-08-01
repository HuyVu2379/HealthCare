package fit.iuh.student.communicationservice.entities;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Document
@Data
@EqualsAndHashCode(of = {"message_id","appointment_id"})
public class Message {
    @Id
    private String message_id;
    private String appointment_id;
    private String sender_id;
    private String receiver_id;
    private String content;
    private Timestamp sendAt;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
