package fit.iut.student.paymentservice.controllers;

import fit.iut.student.paymentservice.dtos.requests.CreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.requests.PayOSWebhookRequest;
import fit.iut.student.paymentservice.dtos.responses.CreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.responses.PaymentStatusResponse;
import fit.iut.student.paymentservice.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/create")
    public ResponseEntity<CreatePaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        try {
            log.info("Received create payment request for appointment: {}", request.getAppointmentId());
            CreatePaymentResponse response = paymentService.createPayment(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error creating payment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handlePayOSWebhook(
            @RequestBody PayOSWebhookRequest webhookRequest
    ) {
        try {
            log.info("Received PayOS webhook");
            paymentService.handlePayOSWebhook(webhookRequest);
            return ResponseEntity.ok("Webhook processed successfully");
        } catch (Exception e) {
            log.error("Error processing webhook: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Webhook processing failed");
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentByAppointmentId(
            @PathVariable String appointmentId
    ) {
        try {
            PaymentStatusResponse response = paymentService.getPaymentByAppointmentId(appointmentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentStatusResponse> getPaymentById(
            @PathVariable String paymentId
    ) {
        try {
            PaymentStatusResponse response = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error getting payment: ", e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<String> refundPayment(
            @PathVariable String paymentId
    ) {
        try {
            log.info("Received refund request for payment: {}", paymentId);
            paymentService.refundPayment(paymentId);
            return ResponseEntity.ok("Payment refunded successfully");
        } catch (Exception e) {
            log.error("Error refunding payment: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Refund failed: " + e.getMessage());
        }
    }
}
