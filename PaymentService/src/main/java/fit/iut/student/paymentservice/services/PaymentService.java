package fit.iut.student.paymentservice.services;

import fit.iut.student.paymentservice.dtos.requests.CreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.responses.CreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.responses.PaymentStatusResponse;

import java.util.Map;

public interface PaymentService {

    /**
     * Create a new payment and generate PayOS payment link
     */
    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    /**
     * Get payment status by appointment ID
     */
    PaymentStatusResponse getPaymentByAppointmentId(String appointmentId);

    /**
     * Get payment status by payment ID
     */
    PaymentStatusResponse getPaymentById(String paymentId);

    /**
     * Refund payment (when doctor rejects appointment)
     */
    void refundPayment(String paymentId);

    /**
     * Cancel payment by payment ID (when user cancels on PayOS payment page)
     */
    void cancelPayment(String paymentId);

    /**
     * Cancel payment by appointment ID (when user cancels on PayOS payment page)
     */
    void cancelPaymentByAppointmentId(String appointmentId);

    /**
     * Cancel payment by order code (when PayOS redirects to cancel-url with orderCode)
     */
    void cancelPaymentByOrderCode(Long orderCode);

    /**
     * Handle PayOS webhook - update payment status from PayOS real-time notification
     */
    void handlePayOSWebhook(Map<String, Object> webhookData);

    /**
     * Check and update expired payments (scheduled job)
     */
    void checkExpiredPayments();
}
