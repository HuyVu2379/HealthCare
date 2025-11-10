package fit.iut.student.paymentservice.dtos.payos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSCreatePaymentRequest {
    private String orderCode;
    private Integer amount;
    private String description;
    private String returnUrl;
    private String cancelUrl;
    private String buyerName;
    private String buyerEmail;
    private String buyerPhone;
    private String buyerAddress;
}
