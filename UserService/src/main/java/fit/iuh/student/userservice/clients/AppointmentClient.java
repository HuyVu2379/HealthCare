package fit.iuh.student.userservice.clients;

import fit.iuh.student.userservice.clients.dtos.AppointmentClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "SchedulingService")
public interface AppointmentClient {
    @GetMapping("/api/v1/appointments/client/{appointmentId}/detail")
    AppointmentClientResponse getAppointmentDetailForClientById(@PathVariable String appointmentId);
}
