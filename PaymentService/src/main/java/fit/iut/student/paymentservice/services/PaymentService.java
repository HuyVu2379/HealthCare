package fit.iut.student.paymentservice.services;

import fit.iut.student.paymentservice.dtos.requests.CreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.requests.PayOSWebhookRequest;
import fit.iut.student.paymentservice.dtos.responses.CreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.responses.PaymentStatusResponse;

public interface PaymentService {

    /**
     * Create a new payment and generate PayOS payment link
     */
    CreatePaymentResponse createPayment(CreatePaymentRequest request);

    /**
     * Handle PayOS webhook notification
     */
    void handlePayOSWebhook(PayOSWebhookRequest webhookRequest);

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
     * Check and update expired payments (scheduled job)
     */
    void checkExpiredPayments();
}
