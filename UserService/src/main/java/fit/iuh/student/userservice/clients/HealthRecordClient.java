package fit.iuh.student.userservice.clients;

import fit.iuh.student.userservice.clients.dtos.HealthMetricClientResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "HealthRecordService")
public interface HealthRecordClient {
    @GetMapping("/api/v1/health-metrics/getEGFRMetric/{patientId}")
    HealthMetricClientResponse getEGFRMetric(@PathVariable String patientId);
}
