package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.ImportHealthMetricsRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.healthrecordservice.services.HealthMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/health-metrics")
@RequiredArgsConstructor
public class HealthMetricController {
    private final HealthMetricService healthMetricService;
    @GetMapping("/getEGFRMetric/{patientId}")
    public HealthMetricClientResponse getEGFRMetric(
           @PathVariable String patientId) {
        return healthMetricService.getEGFRMetric(patientId);
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MessageResponse<HealthMetricResponse>> createHealthMetric(
           @RequestBody CreateHealthMetricRequest request) {
        return SuccessEntityResponse.ok("Create health metric successfully !",healthMetricService.createHealthMetric(request));
    }

    @PostMapping("/import")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MessageResponse<List<HealthMetricResponse>>> importHealthMetrics(
           @RequestBody ImportHealthMetricsRequest request) {
        return SuccessEntityResponse.ok("Import health metrics successfully !",healthMetricService.importHealthMetrics(request));
    }
}
