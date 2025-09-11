package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.responses.StatisticResponse;

import java.util.List;

public interface StatisticalReportService {
    List<StatisticResponse> compareDailyStatistics(String patientId, int month, String metricName);
}
