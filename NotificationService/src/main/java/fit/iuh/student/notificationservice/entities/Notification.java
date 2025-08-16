package fit.iuh.student.notificationservice.entities;

import fit.iuh.student.notificationservice.enums.NotificationType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {
    @Id
    @Column(name = "notification_id")
    private String notificationId;
    
    @Column(name = "recipient_id", nullable = false)
    private String recipient_id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private NotificationType type;
    
    @Column(name = "message", nullable = false)
    private String message;
}
