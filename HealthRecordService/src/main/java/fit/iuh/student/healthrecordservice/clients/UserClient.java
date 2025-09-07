package fit.iuh.student.healthrecordservice.clients;

import fit.iuh.student.healthrecordservice.clients.dtos.PatientClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService")
public interface UserClient {
    @GetMapping("/api/v1/patients/getPatientForClient/{patientId}")
    PatientClientResponse getPatientForClient(@PathVariable String patientId);
}
