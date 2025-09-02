package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.ImportHealthMetricsRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import fit.iuh.student.healthrecordservice.mappers.HealthMetricMapper;
import fit.iuh.student.healthrecordservice.repositories.HealthMetricRepository;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.services.HealthMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HealthMetricServiceImpl implements HealthMetricService {
    private final HealthMetricRepository healthMetricRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final HealthMetricMapper healthMetricMapper;
    @Override
    public HealthMetricClientResponse getEGFRMetric(String patientId) {
        try{
            return healthMetricMapper.toHealthMetricClientResponse(healthMetricRepository.getEGFRMetric(patientId));
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public HealthMetricResponse createHealthMetric(CreateHealthMetricRequest request) {
        try {
            HealthMetric healthMetric = new HealthMetric(
                    request.getPatientId(),
                    request.getMetricName(),
                    request.getMetricValue(),
                    request.getUnit(),
                    medicalRecordRepository.findById(request.getRecordId()).orElse(null),
                    new Date(request.getMeasuredAt().getTime()));
            HealthMetric savedMetric = healthMetricRepository.save(healthMetric);
            return healthMetricMapper.toHealthMetricResponse(savedMetric);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<HealthMetricResponse> importHealthMetrics(ImportHealthMetricsRequest request) {
        try{
            List<HealthMetric> healthMetrics = request.getHealthMetrics().stream()
                    .map(hm ->{
                        HealthMetric healthMetric = new HealthMetric();
                        healthMetric.setPatientId(hm.getPatientId());
                        healthMetric.setMetricName(hm.getMetricName());
                        healthMetric.setMetricValue(hm.getMetricValue());
                        healthMetric.setUnit(hm.getUnit());
                        healthMetric.setMedicalRecord(medicalRecordRepository.findById(hm.getRecordId()).orElse(null));
                        healthMetric.setMeasuredAt(new Date(hm.getMeasuredAt().getTime()));
                        return healthMetric;
                    }).toList();
            List<HealthMetric> hms = healthMetricRepository.saveAll(healthMetrics);
            return hms.stream().map(healthMetricMapper::toHealthMetricResponse).toList();
        } catch (Exception e) {
            throw e;
        }
    }
}
