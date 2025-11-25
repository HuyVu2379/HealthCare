package fit.iuh.student.schedulingservice.clients;

import fit.iuh.student.schedulingservice.clients.dtos.MedicalRecordClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "HealthRecordService")
public interface HealthRecordClient {
    @GetMapping("/api/v1/medical-records/recent-by-doctor/{doctorId}")
    List<MedicalRecordClientResponse> getRecentMedicalRecordsByDoctor(
            @PathVariable String doctorId,
            @RequestParam(defaultValue = "10") int limit
    );
}
