package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.dtos.responses.StatisticResponse;
import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import fit.iuh.student.healthrecordservice.repositories.HealthMetricRepository;
import fit.iuh.student.healthrecordservice.services.StatisticalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticalReportServiceImpl implements StatisticalReportService {
    private final HealthMetricRepository healthMetricRepository;

    @Override
    public List<StatisticResponse> compareDailyStatistics(String patientId, int month, String metricName) {
        try {
            List<HealthMetric> metrics = switch (metricName) {
                case "eGFR" -> healthMetricRepository.findHealthMetricFilterByMetricName(patientId, "eGFR", month);
                case "Creatinine" ->
                        healthMetricRepository.findHealthMetricFilterByMetricName(patientId, "Creatinine", month);
                case "Blood_pressure" ->
                        healthMetricRepository.findHealthMetricFilterByMetricName(patientId, "blood_pressure", month);
                case "Bun" -> healthMetricRepository.findHealthMetricFilterByMetricName(patientId, "Bun", month);
                case "Urine_ph" ->
                        healthMetricRepository.findHealthMetricFilterByMetricName(patientId, "Urine_ph", month);
                default -> healthMetricRepository.findHealthMetricFilterAll(patientId, month);
            };

            List<StatisticResponse> responses = new ArrayList<>();

            if (metrics.isEmpty()) {
                return responses;
            }

//             Nhóm metrics theo ngày
            Map<Date, List<HealthMetric>> metricsByDate = metrics.stream()
                    .collect(Collectors.groupingBy(HealthMetric::getMeasuredAt));

            // Sắp xếp các ngày theo thứ tự tăng dần
            List<Date> sortedDates = metricsByDate.keySet().stream()
                    .sorted()
                    .collect(Collectors.toList());

            // Tạo response cho từng ngày, so sánh với ngày trước đó
            for (int i = 1; i < sortedDates.size(); i++) {
                Date currentDate = sortedDates.get(i);
                Date previousDate = sortedDates.get(i - 1);

                List<HealthMetric> currentMetrics = metricsByDate.get(currentDate);
                List<HealthMetric> previousMetrics = metricsByDate.get(previousDate);

                // Tính toán thay đổi trung bình
                double currentAvg = currentMetrics.stream()
                        .mapToDouble(HealthMetric::getMetricValue)
                        .average()
                        .orElse(0.0);

                double previousAvg = previousMetrics.stream()
                        .mapToDouble(HealthMetric::getMetricValue)
                        .average()
                        .orElse(0.0);

                double changePercent = 0.0;
                boolean isIncrease = false;

                if (previousAvg != 0) {
                    changePercent = Math.abs((currentAvg - previousAvg) / previousAvg * 100);
                    isIncrease = currentAvg > previousAvg;
                }

                // Chuyển đổi HealthMetric sang HeathMetricDTO
                List<StatisticResponse.HeathMetricDTO> metricDTOs = currentMetrics.stream()
                        .map(metric -> StatisticResponse.HeathMetricDTO.builder()
                                .metricId(metric.getMetricId())
                                .metricName(metric.getMetricName())
                                .metricValue(metric.getMetricValue())
                                .unit(metric.getUnit())
                                .measuredAt(metric.getMeasuredAt())
                                .build())
                        .collect(Collectors.toList());

                StatisticResponse response = StatisticResponse.builder()
                        .changePercent(Math.round(changePercent * 100.0) / 100.0) // Làm tròn 2 chữ số thập phân
                        .isIncrease(isIncrease)
                        .currentMetric(metricDTOs)
                        .build();

                responses.add(response);
            }

            return responses;

        } catch (Exception e) {
            throw new RuntimeException("Error calculating daily statistics: " + e.getMessage(), e);
        }
    }
}
