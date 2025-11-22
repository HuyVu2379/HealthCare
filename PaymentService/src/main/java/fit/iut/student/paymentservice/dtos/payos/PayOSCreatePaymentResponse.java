package fit.iut.student.paymentservice.dtos.payos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayOSCreatePaymentResponse {
    private String code; // "00" if success
    private String desc;
    private PayOSPaymentData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PayOSPaymentData {
        private String bin;
        private String accountNumber;
        private String accountName;
        private Integer amount;
        private String description;
        private String orderCode;
        private String currency;
        private String paymentLinkId;
        private String status;
        private String checkoutUrl;
        private String qrCode;
    }
}
