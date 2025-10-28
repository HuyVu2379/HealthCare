package fit.iuh.student.healthrecordservice.clients;

import fit.iuh.student.healthrecordservice.clients.dtos.AppointmentClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SchedulingService")  // Fixed: Must match spring.application.name in SchedulingService
public interface AppointmentClient {

    @GetMapping("/api/v1/appointments/client/{appointmentId}/detail")
    AppointmentClientResponse getAppointmentForClient(@PathVariable("appointmentId") String appointmentId);
}
