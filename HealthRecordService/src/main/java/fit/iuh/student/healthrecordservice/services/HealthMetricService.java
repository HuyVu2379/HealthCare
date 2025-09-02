package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.ImportHealthMetricsRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;

import java.util.List;

public interface HealthMetricService {
    HealthMetricClientResponse getEGFRMetric(String patientId);
    HealthMetricResponse createHealthMetric(CreateHealthMetricRequest healthMetric);
    List<HealthMetricResponse> importHealthMetrics(ImportHealthMetricsRequest request);
}
