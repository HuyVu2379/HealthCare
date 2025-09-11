package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.StatisticResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.healthrecordservice.services.StatisticalReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/statistical-reports")
@RequiredArgsConstructor
public class StatisticalReportController {
    private final StatisticalReportService statisticalReportService;
    @GetMapping("/compare-daily")
    public ResponseEntity<MessageResponse<List<StatisticResponse>>> compareDailyStatistics(
            @RequestParam String patientId,
            @RequestParam int month,
            @RequestParam String metricName
    ) {
        return SuccessEntityResponse.ok("Compare daily statistics successfully !",
                statisticalReportService.compareDailyStatistics(patientId, month, metricName));
    }
}
