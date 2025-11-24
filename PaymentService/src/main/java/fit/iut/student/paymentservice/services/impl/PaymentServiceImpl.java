package fit.iut.student.paymentservice.services.impl;

import fit.iut.student.paymentservice.clients.SchedulingClient;
import fit.iut.student.paymentservice.config.PaymentConfig;
import fit.iut.student.paymentservice.dtos.requests.CreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.responses.CreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.responses.PaymentStatusResponse;
import fit.iut.student.paymentservice.entities.Payment;
import fit.iut.student.paymentservice.repositories.PaymentRepository;
import fit.iut.student.paymentservice.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.exception.ConnectionException;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final SchedulingClient schedulingClient;
    private final PayOS payOS;
    private final PaymentConfig paymentConfig;

    @Override
    @Transactional
    @Retryable(
        retryFor = {ConnectionException.class},
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000, multiplier = 2.0, maxDelay = 5000)
    )
    public CreatePaymentResponse createPayment(CreatePaymentRequest request) {
        try {
            log.info("Creating payment for appointment: {} (Network retry enabled)", request.getAppointmentId());

            // Generate unique payment ID and order code
            String paymentId = UUID.randomUUID().toString();
            Long orderCode = generateOrderCode();

            // Prepare description using payment code (PayOS best practice: short unique code)
            String originalDescription = request.getDescription() != null && !request.getDescription().trim().isEmpty() ?
                    request.getDescription() :
                    generatePaymentCode(request.getAppointmentId());  // Generate "KH{appointmentId}" format
            String truncatedDescription = truncateDescription(originalDescription);

            // Build CreatePaymentLinkRequest using PayOS SDK v2
            CreatePaymentLinkRequest paymentLinkRequest = CreatePaymentLinkRequest.builder()
                    .orderCode(orderCode)
                    .amount(request.getAmount().longValue())
                    .description(truncatedDescription) // Use truncated description
                    .returnUrl(request.getReturnUrl() != null ?
                            request.getReturnUrl() :
                            paymentConfig.getReturnUrl())
                    .cancelUrl(request.getCancelUrl() != null ?
                            request.getCancelUrl() :
                            paymentConfig.getCancelUrl())
                    .build();

            // Call PayOS SDK to create payment link
            log.info("Calling PayOS SDK - OrderCode: {}, Amount: {}, Description: '{}' (KH format: 10 chars)",
                    orderCode, request.getAmount(), truncatedDescription);

            CreatePaymentLinkResponse paymentLinkResponse = payOS.paymentRequests().create(paymentLinkRequest);

            log.info("PayOS SDK response - CheckoutUrl: {}, Status: {}",
                    paymentLinkResponse.getCheckoutUrl(),
                    paymentLinkResponse.getStatus());

            // Calculate expiry time
            LocalDateTime expiresAt = LocalDateTime.now()
                    .plusMinutes(paymentConfig.getExpiryMinutes());

            // Save payment to database
            Payment payment = Payment.builder()
                    .paymentId(paymentId)
                    .appointmentId(request.getAppointmentId())
                    .amount(request.getAmount())
                    .orderCode(orderCode)
                    .paymentUrl(paymentLinkResponse.getCheckoutUrl())
                    .status("PENDING")
                    .paymentMethod("BANK")
                    .createdAt(LocalDateTime.now())
                    .expiresAt(expiresAt)
                    .description(originalDescription)
                    .build();

            paymentRepository.save(payment);

            log.info("Payment created successfully with ID: {}", paymentId);

            return CreatePaymentResponse.builder()
                    .paymentId(paymentId)
                    .appointmentId(request.getAppointmentId())
                    .orderCode(orderCode)
                    .paymentUrl(paymentLinkResponse.getCheckoutUrl())
                    .amount(request.getAmount())
                    .expiresAt(expiresAt)
                    .status("PENDING")
                    .build();

        } catch (ConnectionException e) {
            log.error("Network connection error while creating payment for appointment {}: {}",
                    request.getAppointmentId(), e.getMessage());
            throw new RuntimeException("Lỗi kết nối mạng. Vui lòng kiểm tra internet và thử lại.", e);
        } catch (Exception e) {
            log.error("Error creating payment for appointment {}: ", request.getAppointmentId(), e);
            throw new RuntimeException("Lỗi tạo thanh toán: " + e.getMessage(), e);
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

            payment.setStatus("REFUNDED");
            paymentRepository.save(payment);

            log.info("Payment {} refunded successfully", paymentId);

        } catch (Exception e) {
            log.error("Error refunding payment: ", e);
            throw new RuntimeException("Failed to refund payment: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void cancelPayment(String paymentId) {
        try {
            log.info("[Payment Cancellation] Processing cancellation for payment: {}", paymentId);

            Payment payment = paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found: " + paymentId));

            log.info("[Payment Cancellation] Found payment - Current status: {}, AppointmentId: {}",
                    payment.getStatus(), payment.getAppointmentId());

            // Validate: Cannot cancel already paid payment
            if ("PAID".equals(payment.getStatus())) {
                throw new RuntimeException("Cannot cancel already paid payment");
            }

            // Check if already cancelled or expired
            if ("CANCELLED".equals(payment.getStatus()) || "EXPIRED".equals(payment.getStatus())) {
                log.warn("[Payment Cancellation] Payment {} already in final status: {}. Skipping update.",
                        paymentId, payment.getStatus());
                return;
            }

            // Update payment status to CANCELLED
            payment.setStatus("CANCELLED");
            paymentRepository.save(payment);
            paymentRepository.flush();

            log.info("[Payment Cancellation] ✓ Payment {} marked as CANCELLED in database", paymentId);

            // Cancel appointment
            try {
                schedulingClient.updateAppointmentStatus(payment.getAppointmentId(), "CANCELED");
                log.info("[Payment Cancellation] ✓ Appointment {} status updated to CANCELED",
                        payment.getAppointmentId());
            } catch (Exception e) {
                log.error("[Payment Cancellation] Error updating appointment status: ", e);
                // Don't throw - payment cancellation should persist even if appointment update fails
            }

        } catch (Exception e) {
            log.error("[Payment Cancellation] Error cancelling payment: ", e);
            throw new RuntimeException("Failed to cancel payment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelPaymentByAppointmentId(String appointmentId) {
        try {
            log.info("[Payment Cancellation] Processing cancellation for appointment: {}", appointmentId);

            Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found for appointment: " + appointmentId));

            log.info("[Payment Cancellation] Found payment {} for appointment {} - Current status: {}",
                    payment.getPaymentId(), appointmentId, payment.getStatus());

            // Delegate to cancelPayment
            cancelPayment(payment.getPaymentId());

        } catch (Exception e) {
            log.error("[Payment Cancellation] Error cancelling payment for appointment {}: ", appointmentId, e);
            throw new RuntimeException("Failed to cancel payment for appointment: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void cancelPaymentByOrderCode(Long orderCode) {
        try {
            log.info("[Payment Cancellation] Processing cancellation for orderCode: {}", orderCode);

            Payment payment = paymentRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Payment not found for orderCode: " + orderCode));

            log.info("[Payment Cancellation] Found payment {} for orderCode {} - Current status: {}",
                    payment.getPaymentId(), orderCode, payment.getStatus());

            // Delegate to cancelPayment
            cancelPayment(payment.getPaymentId());

        } catch (Exception e) {
            log.error("[Payment Cancellation] Error cancelling payment for orderCode {}: ", orderCode, e);
            throw new RuntimeException("Failed to cancel payment for orderCode: " + e.getMessage(), e);
        }
    }

    
    //  * Handle PayOS Webhook
    @Override
    @Transactional
    public void handlePayOSWebhook(Map<String, Object> webhookBody) {
        try {
            // Extract data from webhook body
            @SuppressWarnings("unchecked")
            Map<String, Object> data = (Map<String, Object>) webhookBody.get("data");

            if (data == null) {
                throw new RuntimeException("Webhook data is null");
            }

            // Extract orderCode from data
            Object orderCodeObj = data.get("orderCode");
            Long orderCode = orderCodeObj instanceof Number ?
                    ((Number) orderCodeObj).longValue() :
                    Long.parseLong(orderCodeObj.toString());

            // Find payment by orderCode
            Payment payment = paymentRepository.findByOrderCode(orderCode)
                    .orElseThrow(() -> new RuntimeException("Payment not found for orderCode: " + orderCode));

            // Check if already processed
            if ("PAID".equals(payment.getStatus()) || "CANCELLED".equals(payment.getStatus())) {
                log.warn("[PayOS Webhook] Payment {} already processed with status: {}. Skipping update.",
                        payment.getPaymentId(), payment.getStatus());
                return;
            }

            // Extract payment status from webhook
            String code = (String) webhookBody.get("code");
            String status = (String) data.get("status");

            // Determine if payment is successful
            boolean isSuccess = "00".equals(code) || "PAID".equals(status);

            if (isSuccess) {
                // Payment successful
                payment.setStatus("PAID");
                payment.setPaidAt(LocalDateTime.now());
                payment.setPaymentMethod("BANK");

                // Set transaction ID from webhook
                String paymentLinkId = (String) data.get("id");
                if (paymentLinkId != null) {
                    payment.setTransactionId(paymentLinkId);
                }

                paymentRepository.save(payment);
                paymentRepository.flush();

                // Update appointment status
                try {
                    schedulingClient.updatePaymentStatus(payment.getAppointmentId(), "PAID");
                    schedulingClient.updateAppointmentStatus(payment.getAppointmentId(), "PENDING");
                } catch (Exception e) {
                    log.error("[PayOS Webhook] Error updating appointment status: ", e);
                    // Don't throw - payment update should persist even if appointment update fails
                }

            } else {
                // Payment failed or cancelled
                log.info("[PayOS Webhook] Payment {} cancelled - Code: {}, Status: {}",
                        payment.getPaymentId(), code, status);

                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);
                paymentRepository.flush();

                log.info("[PayOS Webhook] ✓ Payment {} marked as CANCELLED in database",
                        payment.getPaymentId());

                // Cancel appointment
                try {
                    schedulingClient.updateAppointmentStatus(payment.getAppointmentId(), "CANCELED");
                    log.info("[PayOS Webhook] Canceled appointment {} due to payment cancellation",
                            payment.getAppointmentId());
                } catch (Exception e) {
                    log.error("[PayOS Webhook] Error canceling appointment: ", e);
                }
            }

        } catch (Exception e) {
            log.error("[PayOS Webhook] Error processing webhook: ", e);
            throw new RuntimeException("Failed to process webhook: " + e.getMessage(), e);
        }
    }

    @Override
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void checkExpiredPayments() {
        try {
            List<Payment> expiredPayments = paymentRepository.findExpiredPayments(LocalDateTime.now());

            for (Payment payment : expiredPayments) {
                payment.setStatus("EXPIRED");
                paymentRepository.save(payment);
                log.info("Payment {} marked as EXPIRED", payment.getPaymentId());

                // Cancel appointment with status PAYMENT_PENDING → CANCELED
                try {
                    schedulingClient.updateAppointmentStatus(payment.getAppointmentId(), "CANCELED");
                    log.info("Canceled appointment {} due to payment expiry", payment.getAppointmentId());
                } catch (Exception e) {
                    log.error("Error canceling appointment {}: {}", payment.getAppointmentId(), e.getMessage());
                    // Continue with next payment
                }
            }

            if (!expiredPayments.isEmpty()) {
                log.info("Marked {} payments as expired and canceled their appointments", expiredPayments.size());
            }

        } catch (Exception e) {
            log.error("Error checking expired payments: ", e);
        }
    }

    // Helper methods

    private String truncateDescription(String description) {
        final int MAX_LENGTH = 25;

        // Should not happen, but safety check
        if (description == null || description.trim().isEmpty()) {
            return "DH" + System.currentTimeMillis() / 1000;
        }

        String trimmed = description.trim();

        // Most common case: payment code is already short
        if (trimmed.length() <= MAX_LENGTH) {
            return trimmed;
        }

        // Only if frontend sends custom long description
        String truncated = trimmed.substring(0, MAX_LENGTH);
        log.warn("Description truncated from '{}' to '{}' (PayOS 25-char limit)",
                trimmed, truncated);
        return truncated;
    }

    private Long generateOrderCode() {
        return System.currentTimeMillis() / 1000;
    }

//    Generate payment code for PayOS description (max 25 chars);

    private String generatePaymentCode(String appointmentId) {
        String shortId = appointmentId.replace("-", "").substring(0, 8).toUpperCase();
        return "KH" + shortId; 
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
