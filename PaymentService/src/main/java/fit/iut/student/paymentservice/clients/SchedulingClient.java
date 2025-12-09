package fit.iut.student.paymentservice.clients;

import fit.iut.student.paymentservice.config.FeignAuthConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "SchedulingService",
        configuration = FeignAuthConfig.class
)
public interface SchedulingClient {

    @PutMapping("/api/v1/appointments/updatePaymentStatus/{appointmentId}")
    ResponseEntity<Void> updatePaymentStatus(
            @PathVariable("appointmentId") String appointmentId,
            @RequestParam("paymentStatus") String paymentStatus
    );

    @PutMapping("/api/v1/appointments/updateStatus/{appointmentId}")
    ResponseEntity<Void> updateAppointmentStatus(
            @PathVariable("appointmentId") String appointmentId,
            @RequestParam("status") String status
    );
}
