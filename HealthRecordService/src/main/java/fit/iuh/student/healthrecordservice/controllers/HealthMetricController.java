package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.ImportHealthMetricsRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricPanelRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.*;
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

    @PostMapping("/create-panel")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<MessageResponse<List<HealthMetricResponse>>> createPanel(
           @RequestBody CreateHealthMetricPanelRequest request) {
        ImportHealthMetricsRequest mapped = new ImportHealthMetricsRequest();
        ImportHealthMetricsRequest.HealthMetricData dataTemplate = new ImportHealthMetricsRequest.HealthMetricData();
        List<ImportHealthMetricsRequest.HealthMetricData> items = request.getMetrics().stream().map(m -> {
            ImportHealthMetricsRequest.HealthMetricData item = new ImportHealthMetricsRequest.HealthMetricData();
            item.setPatientId(request.getPatientId());
            item.setRecordId(request.getRecordId());
            item.setMeasuredAt(request.getMeasuredAt());
            item.setMetricName(m.getName());
            item.setMetricValue(m.getValue());
            item.setUnit(m.getUnit());
            return item;
        }).toList();
        mapped.setHealthMetrics(items);
        return SuccessEntityResponse.ok("Create health metric panel successfully !",healthMetricService.importHealthMetrics(mapped));
    }

    @GetMapping("/by-patient")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<MessageResponse<List<HealthMetricPanelResponse>>> getPanelsByPatient(
           @RequestParam String patientId) {
        return SuccessEntityResponse.ok("Get health metric panels successfully !",healthMetricService.getPanelsByPatient(patientId));
    }

    @GetMapping("/by-patient-and-date")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<MessageResponse<List<HealthMetricPanelResponse>>> getPanelsByPatientAndDate(
           @RequestParam String patientId,
           @RequestParam java.sql.Date measuredAt) {
        return SuccessEntityResponse.ok("Get health metric panels by date successfully !",healthMetricService.getPanelsByPatientAndDate(patientId, measuredAt));
    }

    @GetMapping("/get-health-metrics-latest/{patientId}")
    public List<HealthMetricResponse> getMetricLatestByPatientId(
            @PathVariable String patientId) {
        return healthMetricService.getMetricByPatientId(patientId);
    }

    @PostMapping("/create-health-metrics")
    public List<HealthMetricResponse> createHealthMetrics(
            @RequestBody List<CreateHealthMetricRequest> healthMetrics) {
        return healthMetricService.createHealthMetrics(healthMetrics);
    }

    @GetMapping("/get-health-metrics-with-batch/{patientId}")
    public List<HealthMetricResponseWithBatch> getHealthMetricsWithBatch(
            @PathVariable String patientId) {
        return healthMetricService.getHealthMetricsWithBatch(patientId);
    }
}
