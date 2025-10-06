package fit.iuh.student.schedulingservice.clients;

import fit.iuh.student.schedulingservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.schedulingservice.dtos.responses.HealthMetricResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "GatewayService")
public interface ScheduleClient {
    @GetMapping(value = "/api/v1/health-metrics/get-health-metrics-latest/{patientId}")
    List<HealthMetricResponse> getHealthMetricsByPatientIdClient(
            @PathVariable("patientId") String patientId
//            @RequestHeader("Authorization") String authorization
    );


    @PostMapping(value = "/api/v1/health-metrics/create-health-metrics")
    List<HealthMetricResponse> createHealthMetrics(
            @RequestBody List<CreateHealthMetricRequest> healthMetrics,
            @RequestHeader("Authorization") String authorization
    );
}
