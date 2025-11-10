package fit.iut.student.paymentservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "scheduling-service")
public interface SchedulingClient {

    @PutMapping("/api/v1/appointments/updatePaymentStatus/{appointmentId}")
    ResponseEntity<Void> updatePaymentStatus(
            @PathVariable("appointmentId") String appointmentId,
            @RequestParam("paymentStatus") String paymentStatus
    );
}
