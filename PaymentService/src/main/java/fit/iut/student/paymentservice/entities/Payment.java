package fit.iut.student.paymentservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @Column(name = "payment_id", length = 36)
    private String paymentId;

    @Column(name = "appointment_id", nullable = false, length = 36)
    private String appointmentId;

    @Column(name = "amount", nullable = false)
    private Long amount;  

    @Column(name = "order_code", unique = true)
    private Long orderCode; 

    @Column(name = "payment_url", length = 500)
    private String paymentUrl;

    @Column(name = "status", nullable = false, length = 20)
    private String status; 

    @Column(name = "payment_method", length = 50)
    private String paymentMethod; 

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "transaction_id", length = 100)
    private String transactionId;

    @Column(name = "description", length = 500)
    private String description;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
