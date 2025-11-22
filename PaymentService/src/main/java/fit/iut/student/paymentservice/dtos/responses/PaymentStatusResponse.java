package fit.iut.student.paymentservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusResponse {
    private String paymentId;
    private String appointmentId;
    private Long orderCode;  
    private Long amount; 
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime paidAt;
    private LocalDateTime expiresAt;
    private String transactionId;
}
