package fit.iut.student.paymentservice.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    @NotBlank(message = "Appointment ID is required")
    private String appointmentId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    private Long amount; 

    private String description;

    private String returnUrl;

    private String cancelUrl;
}
