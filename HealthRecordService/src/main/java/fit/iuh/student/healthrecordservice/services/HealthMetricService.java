package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.ImportHealthMetricsRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricPanelResponse;

import java.util.List;

public interface HealthMetricService {
    HealthMetricClientResponse getEGFRMetric(String patientId);
    HealthMetricResponse createHealthMetric(CreateHealthMetricRequest healthMetric);
    List<HealthMetricResponse> importHealthMetrics(ImportHealthMetricsRequest request);
    List<HealthMetricPanelResponse> getPanelsByPatient(String patientId);
    List<HealthMetricPanelResponse> getPanelsByPatientAndDate(String patientId, java.sql.Date measuredAt);
    List<HealthMetricResponse> getMetricByPatientId(String patientId);
    List<HealthMetricResponse> createHealthMetrics(List<CreateHealthMetricRequest> healthMetrics);
}
