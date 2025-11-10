package fit.iut.student.paymentservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSWebhookRequest {
    private String orderCode;
    private Integer amount;
    private String description;
    private String accountNumber;
    private String reference;
    private String transactionDateTime;
    private String virtualAccountName;
    private String virtualAccountNumber;
    private String counterAccountBankId;
    private String counterAccountBankName;
    private String counterAccountName;
    private String counterAccountNumber;
    private String code; // Success code from PayOS
    private String desc; // Description from PayOS
    private String data; // Additional data
    private String signature; // HMAC signature for verification
}
