package fit.iut.student.paymentservice.controllers;

import fit.iut.student.paymentservice.dtos.requests.CreatePaymentRequest;
import fit.iut.student.paymentservice.dtos.responses.CreatePaymentResponse;
import fit.iut.student.paymentservice.dtos.responses.MessageResponse;
import fit.iut.student.paymentservice.dtos.responses.PaymentStatusResponse;
import fit.iut.student.paymentservice.dtos.responses.SuccessEntityResponse;
import fit.iut.student.paymentservice.services.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
//import vn.payos.PayOS;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;
//    private final PayOS payOS;

    @PostMapping("/create")
    public ResponseEntity<MessageResponse<CreatePaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        try {
            log.info("Received create payment request for appointment: {}", request.getAppointmentId());
            CreatePaymentResponse response = paymentService.createPayment(request);
            return SuccessEntityResponse.created("Tạo thanh toán thành công", response);
        } catch (Exception e) {
            log.error("Error creating payment: ", e);
            MessageResponse<CreatePaymentResponse> errorResponse = new MessageResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Không thể tạo thanh toán: " + e.getMessage(),
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<MessageResponse<PaymentStatusResponse>> getPaymentByAppointmentId(
            @PathVariable String appointmentId
    ) {
        try {
            log.info("[Frontend Polling] Getting payment status for appointment: {}", appointmentId);
            PaymentStatusResponse response = paymentService.getPaymentByAppointmentId(appointmentId);
            log.info("[Frontend Polling] Payment found - Status: {}, PaymentId: {}",
                    response.getStatus(), response.getPaymentId());
            return SuccessEntityResponse.ok("Lấy trạng thái thanh toán thành công", response);
        } catch (Exception e) {
            log.error("[Frontend Polling] Error getting payment for appointment {}: {}",
                    appointmentId, e.getMessage());
            MessageResponse<PaymentStatusResponse> errorResponse = new MessageResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Không tìm thấy thanh toán cho appointment: " + appointmentId,
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<MessageResponse<PaymentStatusResponse>> getPaymentById(
            @PathVariable String paymentId
    ) {
        try {
            log.info("Getting payment by ID: {}", paymentId);
            PaymentStatusResponse response = paymentService.getPaymentById(paymentId);
            return SuccessEntityResponse.ok("Lấy thông tin thanh toán thành công", response);
        } catch (Exception e) {
            log.error("Error getting payment {}: {}", paymentId, e.getMessage());
            MessageResponse<PaymentStatusResponse> errorResponse = new MessageResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Không tìm thấy thanh toán: " + paymentId,
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<MessageResponse<Void>> refundPayment(
            @PathVariable String paymentId
    ) {
        try {
            log.info("Received refund request for payment: {}", paymentId);
            paymentService.refundPayment(paymentId);
            return SuccessEntityResponse.ok("Hoàn tiền thành công", null);
        } catch (Exception e) {
            log.error("Error refunding payment: ", e);
            MessageResponse<Void> errorResponse = new MessageResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Hoàn tiền thất bại: " + e.getMessage(),
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }


//  Cancel Payment by Payment ID

    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<MessageResponse<Void>> cancelPayment(
            @PathVariable String paymentId
    ) {
        try {
            log.info("[Payment Cancellation] Received cancellation request for payment: {}", paymentId);
            paymentService.cancelPayment(paymentId);
            return SuccessEntityResponse.ok("Hủy thanh toán thành công", null);
        } catch (Exception e) {
            log.error("[Payment Cancellation] Error cancelling payment: ", e);
            MessageResponse<Void> errorResponse = new MessageResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Hủy thanh toán thất bại: " + e.getMessage(),
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Cancel Payment by Appointment ID
     * Convenience endpoint for frontend to cancel using appointmentId
     * Frontend gets redirected with appointmentId in query params
     */
    @PostMapping("/cancel/appointment/{appointmentId}")
    public ResponseEntity<MessageResponse<Void>> cancelPaymentByAppointmentId(
            @PathVariable String appointmentId
    ) {
        try {
            log.info("[Payment Cancellation] Received cancellation request for appointment: {}", appointmentId);
            paymentService.cancelPaymentByAppointmentId(appointmentId);
            return SuccessEntityResponse.ok("Hủy thanh toán thành công", null);
        } catch (Exception e) {
            log.error("[Payment Cancellation] Error cancelling payment for appointment: ", e);
            MessageResponse<Void> errorResponse = new MessageResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Hủy thanh toán thất bại: " + e.getMessage(),
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * Cancel Payment by Order Code
     * Main endpoint for handling PayOS cancel-url redirects
     * PayOS redirects to cancel-url with orderCode in query params
     */
    @PostMapping("/cancel/orderCode/{orderCode}")
    public ResponseEntity<MessageResponse<Void>> cancelPaymentByOrderCode(
            @PathVariable Long orderCode
    ) {
        try {
            log.info("[Payment Cancellation] Received cancellation request for orderCode: {}", orderCode);
            paymentService.cancelPaymentByOrderCode(orderCode);
            return SuccessEntityResponse.ok("Hủy thanh toán thành công", null);
        } catch (Exception e) {
            log.error("[Payment Cancellation] Error cancelling payment for orderCode: ", e);
            MessageResponse<Void> errorResponse = new MessageResponse<>(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Hủy thanh toán thất bại: " + e.getMessage(),
                    false,
                    null
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }

    /**
     * PayOS Webhook Handler
     * Endpoint: POST /api/v1/payments/webhook
     * Receives payment status updates from PayOS in real-time
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleWebhook(
            @RequestBody Map<String, Object> webhookBody
    ) {
        try {
            log.info("[PayOS Webhook] Received webhook from PayOS");
            log.debug("[PayOS Webhook] Webhook body: {}", webhookBody);

            // Process webhook data
            paymentService.handlePayOSWebhook(webhookBody);

            // Return success response to PayOS
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Webhook processed successfully"
            ));

        } catch (Exception e) {
            log.error("[PayOS Webhook] Error processing webhook: ", e);
            // Return error but still 200 OK to prevent PayOS from retrying
            return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Webhook processing failed: " + e.getMessage()
            ));
        }
    }
}
