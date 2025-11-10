package fit.iut.student.paymentservice.services.impl;

import fit.iut.student.paymentservice.clients.SchedulingClient;
import fit.iut.student.paymentservice.config.PayOSConfig;
import fit.iut.student.paymentservice.config.PaymentConfig;
import fit.iut.student.paymentservice.dtos.payos.PayOSCreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.payos.PayOSCreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.requests.CreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.requests.PayOSWebhookRequest;
import fit.iut.student.paymentservice.dtos.responses.CreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.responses.PaymentStatusResponse;
import fit.iut.student.paymentservice.entities.Payment;
import fit.iut.student.paymentservice.repositories.PaymentRepository;
import fit.iut.student.paymentservice.services.PaymentService;
import fit.iut.student.paymentservice.utils.PayOSSignatureUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SchedulingClient schedulingClient;
    private final PayOSConfig payOSConfig;
    private final PaymentConfig paymentConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    @Transactional
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        try {
            log.info("Creating payment for appointment: {}", request.getAppointmentId());

            // Generate unique payment ID and order code
            String paymentId = UUID.randomUUID().toString();
            String orderCode = generateOrderCode();

            // Create PayOS payment request
            PayOSCreatePaymentRequest payOSRequest = PayOSCreatePaymentRequest.builder()
                    .orderCode(orderCode)
                    .amount(request.getAmount())
                    .description(request.getDescription() != null ?
                            request.getDescription() :
                            "Thanh toan kham benh - " + request.getAppointmentId())
                    .returnUrl(request.getReturnUrl() != null ?
                            request.getReturnUrl() :
                            paymentConfig.getReturnUrl())
                    .cancelUrl(request.getCancelUrl() != null ?
                            request.getCancelUrl() :
                            paymentConfig.getCancelUrl())
                    .build();

            // Call PayOS API to create payment link
            PayOSCreatePaymentResponse payOSResponse = callPayOSCreatePayment(payOSRequest);

            if (payOSResponse == null || !("00".equals(payOSResponse.getCode()))) {
                throw new RuntimeException("Failed to create payment link from PayOS");
            }

            // Calculate expiry time
            LocalDateTime expiresAt = LocalDateTime.now()
                    .plusMinutes(paymentConfig.getExpiryMinutes());

            // Save payment to database
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .appointmentId(request.getAppointmentId())
                    .amount(request.getAmount())
                    .orderCode(orderCode)
                    .paymentUrl(payOSResponse.getData().getCheckoutUrl())
                    .status("PENDING")
                    .paymentMethod("PayOS")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .description(request.getDescription())
                    .build();

            paymentRepository.save(payment);

            log.info("Payment created successfully with ID: {}", paymentId);

            return CreatePaymentResponse.builder()
                    .paymentId(paymentId)
                    .appointmentId(request.getAppointmentId())
                    .orderCode(orderCode)
                    .paymentUrl(payOSResponse.getData().getCheckoutUrl())
                    .amount(request.getAmount())
                    .expiresAt(expiresAt)
                    .status("PENDING")
                    .build();

        } catch (Exception e) {
            log.error("Error creating payment: ", e);
            throw new RuntimeException("Failed to create payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handlePayOSWebhook(PayOSWebhookRequest webhookRequest) {
        try {
            log.info("Received PayOS webhook for order code: {}", webhookRequest.getOrderCode());

            // Verify webhook signature
            String dataString = PayOSSignatureUtils.createWebhookDataString(
                    webhookRequest.getOrderCode(),
                    webhookRequest.getAmount(),
                    webhookRequest.getDescription(),
                    webhookRequest.getTransactionDateTime()
            );

            boolean isValid = PayOSSignatureUtils.verifySignature(
                    dataString,
                    webhookRequest.getSignature(),
                    payOSConfig.getChecksumKey()
            );

            if (!isValid) {
                log.error("Invalid webhook signature for order code: {}", webhookRequest.getOrderCode());
                throw new RuntimeException("Invalid webhook signature");
            }

            // Find payment by order code
            Payment payment = paymentRepository.findByOrderCode(webhookRequest.getOrderCode())
                    .orElseThrow(() -> new RuntimeException("Payment not found for order code: " +
                            webhookRequest.getOrderCode()));

            // Check if payment is success (code = "00")
            if ("00".equals(webhookRequest.getCode())) {
                // Update payment status to PAID
                payment.setStatus("PAID");
                payment.setPaidAt(LocalDateTime.now());
                payment.setTransactionId(webhookRequest.getReference());
                paymentRepository.save(payment);

                log.info("Payment {} marked as PAID", payment.getPaymentId());

                // QUAN TRỌNG: Chỉ update paymentStatus = PAID, KHÔNG update status
                // Appointment status VẪN LÀ PENDING chờ bác sĩ confirm
                try {
                    schedulingClient.updatePaymentStatus(payment.getAppointmentId(), "PAID");
                    log.info("Updated appointment {} payment status to PAID", payment.getAppointmentId());
                } catch (Exception e) {
                    log.error("Error updating appointment payment status: ", e);
                    // Continue anyway, payment is still saved
                }

                // TODO: Gửi notification cho bác sĩ qua CommunicationService
                // để bác sĩ xem xét và confirm/reject appointment
                log.info("TODO: Send notification to doctor for appointment: {}",
                        payment.getAppointmentId());

            } else {
                // Payment failed
                payment.setStatus("FAILED");
                paymentRepository.save(payment);
                log.info("Payment {} marked as FAILED", payment.getPaymentId());
            }

        } catch (Exception e) {
            log.error("Error handling PayOS webhook: ", e);
            throw new RuntimeException("Failed to handle webhook: " + e.getMessage());
        }
    }

    @Override
    public PaymentStatusResponse getPaymentByAppointmentId(String appointmentId) {
        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Payment not found for appointment: " + appointmentId));
        return mapToPaymentStatusResponse(payment);
    }

    @Override
    public PaymentStatusResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));
        return mapToPaymentStatusResponse(payment);
    }

    @Override
    @Transactional
    public void refundPayment(String paymentId) {
        try {
            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            if (!"PAID".equals(payment.getStatus())) {
                throw new RuntimeException("Cannot refund payment that is not paid");
            }

            // TODO: Call PayOS refund API
            // For now, just update status to REFUNDED
            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);

            log.info("Payment {} refunded successfully", paymentId);

        } catch (Exception e) {
            log.error("Error refunding payment: ", e);
            throw new RuntimeException("Failed to refund payment: " + e.getMessage());
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000) // Run every minute
    @Transactional
    public void checkExpiredPayments() {
        try {
            List<Payment> expiredPayments = paymentRepository.findExpiredPayments(LocalDateTime.now());

            for (Payment payment : expiredPayments) {
                payment.setStatus("EXPIRED");
                paymentRepository.save(payment);
                log.info("Payment {} marked as EXPIRED", payment.getPaymentId());

                // TODO: Cancel appointment if needed
            }

            if (!expiredPayments.isEmpty()) {
                log.info("Marked {} payments as expired", expiredPayments.size());
            }

        } catch (Exception e) {
            log.error("Error checking expired payments: ", e);
        }
    }

    // Helper methods

    private PayOSCreatePaymentResponse callPayOSCreatePayment(PayOSCreatePaymentRequest request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-client-id", payOSConfig.getClientId());
            headers.set("x-api-key", payOSConfig.getApiKey());

            HttpEntity<PayOSCreatePaymentRequest> entity = new HttpEntity<>(request, headers);

            String url = payOSConfig.getApiUrl() + "/v2/payment-requests";

            return restTemplate.postForObject(url, entity, PayOSCreatePaymentResponse.class);

        } catch (Exception e) {
            log.error("Error calling PayOS API: ", e);
            throw new RuntimeException("Failed to call PayOS API: " + e.getMessage());
        }
    }

    private String generateOrderCode() {
        // Generate unique order code (timestamp + random)
        return System.currentTimeMillis() + "" + (int)(Math.random() * 10000);
    }

    private PaymentStatusResponse mapToPaymentStatusResponse(Payment payment) {
        return PaymentStatusResponse.builder()
                .paymentId(payment.getPaymentId())
                .appointmentId(payment.getAppointmentId())
                .orderCode(payment.getOrderCode())
                .amount(payment.getAmount())
                .status(payment.getStatus())
                .paymentMethod(payment.getPaymentMethod())
                .createdAt(payment.getCreatedAt())
                .paidAt(payment.getPaidAt())
                .expiresAt(payment.getExpiresAt())
                .transactionId(payment.getTransactionId())
                .build();
    }
}
