package fit.iuh.student.schedulingservice.clients;

import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.clients.dtos.PatientClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "UserService")
public interface UserClient {
    @GetMapping("/api/v1/doctors/getDoctorForClient/{doctorId}")
    DoctorClientResponse getDoctorForClient(@PathVariable String doctorId);
    @GetMapping("/api/v1/patients/getPatientForClient/{patientId}")
    PatientClientResponse getPatientForClient(@PathVariable String patientId);
}
