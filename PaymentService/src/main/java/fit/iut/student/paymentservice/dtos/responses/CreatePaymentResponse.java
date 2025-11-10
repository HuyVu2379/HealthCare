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
public class CreatePaymentResponse {
    private String paymentId;
    private String appointmentId;
    private String orderCode;
    private String paymentUrl;
    private Integer amount;
    private LocalDateTime expiresAt;
    private String status;
}
