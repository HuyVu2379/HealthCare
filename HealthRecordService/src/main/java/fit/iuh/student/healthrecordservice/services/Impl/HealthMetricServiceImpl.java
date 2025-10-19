package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateHealthMetricRequest;
import fit.iuh.student.healthrecordservice.dtos.requests.ImportHealthMetricsRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricPanelResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponseWithBatch;
import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import fit.iuh.student.healthrecordservice.mappers.HealthMetricMapper;
import fit.iuh.student.healthrecordservice.repositories.HealthMetricRepository;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.services.HealthMetricService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Objects;

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
                    (request.getRecordId() == null || request.getRecordId().isBlank())
                            ? null
                            : medicalRecordRepository.findById(request.getRecordId()).orElse(null),
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
                        healthMetric.setMedicalRecord(
                                (hm.getRecordId() == null || hm.getRecordId().isBlank())
                                        ? null
                                        : medicalRecordRepository.findById(hm.getRecordId()).orElse(null)
                        );
                        healthMetric.setMeasuredAt(new Date(hm.getMeasuredAt().getTime()));
                        return healthMetric;
                    }).toList();
            List<HealthMetric> hms = healthMetricRepository.saveAll(healthMetrics);
            return hms.stream().map(healthMetricMapper::toHealthMetricResponse).toList();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<HealthMetricPanelResponse> getPanelsByPatient(String patientId) {
        try{
            List<HealthMetric> metrics = healthMetricRepository.findByPatientIdOrderByMeasuredAtDesc(patientId);
            Map<Date, List<HealthMetric>> grouped = metrics.stream()
                    .collect(Collectors.groupingBy(HealthMetric::getMeasuredAt));
            return grouped.entrySet().stream()
                    .sorted((a,b) -> b.getKey().compareTo(a.getKey()))
                    .map(e -> HealthMetricPanelResponse.builder()
                            .measuredAt(e.getKey())
                            .metrics(e.getValue().stream().map(m -> HealthMetricPanelResponse.Item.builder()
                                    .name(m.getMetricName())
                                    .value(m.getMetricValue())
                                    .unit(m.getUnit())
                                    .build()).toList())
                            .build())
                    .toList();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<HealthMetricPanelResponse> getPanelsByPatientAndDate(String patientId, Date measuredAt) {
        try{
            List<HealthMetric> metrics = healthMetricRepository.findByPatientIdAndMeasuredAtOrderByMetricNameAsc(patientId, measuredAt);
            if(metrics.isEmpty()) return List.of();
            HealthMetricPanelResponse panel = HealthMetricPanelResponse.builder()
                    .measuredAt(measuredAt)
                    .metrics(metrics.stream().map(m -> HealthMetricPanelResponse.Item.builder()
                            .name(m.getMetricName())
                            .value(m.getMetricValue())
                            .unit(m.getUnit())
                            .build()).toList())
                    .build();
            return List.of(panel);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<HealthMetricResponse> getMetricByPatientId(String patientId) {
        try {
            List<HealthMetric> allMetrics = healthMetricRepository.findByPatientIdOrderByMeasuredAtDesc(patientId);

            // Group by metric name and get the latest one for each metric type
            Map<String, HealthMetric> latestMetrics = allMetrics.stream()
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(
                            HealthMetric::getMetricName,
                            metric -> metric,
                            (existing, replacement) ->
                                existing.getMeasuredAt().compareTo(replacement.getMeasuredAt()) >= 0
                                    ? existing : replacement
                    ));

            return latestMetrics.values().stream()
                    .sorted((a, b) -> b.getMeasuredAt().compareTo(a.getMeasuredAt()))
                    .map(healthMetricMapper::toHealthMetricResponse)
                    .toList();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<HealthMetricResponse> createHealthMetrics(List<CreateHealthMetricRequest> healthMetrics) {
        try{
            List<HealthMetric> metrics = healthMetrics.stream()
                    .map(hm -> {
                        HealthMetric healthMetric = new HealthMetric();
                        healthMetric.setPatientId(hm.getPatientId());
                        healthMetric.setMetricName(hm.getMetricName());
                        healthMetric.setMetricValue(hm.getMetricValue());
                        healthMetric.setUnit(hm.getUnit());
                        healthMetric.setMedicalRecord(
                                (hm.getRecordId() == null || hm.getRecordId().isBlank())
                                        ? null
                                        : medicalRecordRepository.findById(hm.getRecordId()).orElse(null)
                        );
                        healthMetric.setMeasuredAt(new Date(hm.getMeasuredAt().getTime()));
                        return healthMetric;
                    }).toList();
            List<HealthMetric> savedMetrics = healthMetricRepository.saveAll(metrics);
            return savedMetrics.stream().map(healthMetricMapper::toHealthMetricResponse).toList();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public List<HealthMetricResponseWithBatch> getHealthMetricsWithBatch(String patientId) {
        try{
            List<HealthMetric> metrics = healthMetricRepository.findByPatientIdOrderByMeasuredAtDesc(patientId);
            Map<Date, List<HealthMetric>> grouped = metrics.stream()
                    .collect(Collectors.groupingBy(HealthMetric::getMeasuredAt));
            return grouped.entrySet().stream()
                    .sorted((a,b) -> b.getKey().compareTo(a.getKey()))
                    .limit(2)
                    .map(e -> HealthMetricResponseWithBatch.builder()
                            .measuredAt(e.getKey().toLocalDate().atStartOfDay())
                            .healthMetrics(e.getValue().stream()
                                    .map(healthMetricMapper::toHealthMetricResponse)
                                    .toList())
                            .build())
                    .toList();
        } catch (Exception e) {
            throw e;
        }
    }
}
