package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.responses.DoctorDashboardResponse;
import fit.iuh.student.schedulingservice.dtos.responses.MessageResponse;
import fit.iuh.student.schedulingservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.schedulingservice.services.DoctorDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DoctorDashboardController {
    private final DoctorDashboardService doctorDashboardService;

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MessageResponse<DoctorDashboardResponse>> getDoctorDashboard(
            @PathVariable String doctorId,
            @RequestParam(required = false) String date
    ) {
        // Parse date or use today as default
        Date queryDate = date != null
                ? Date.valueOf(date)
                : Date.valueOf(LocalDate.now());

        DoctorDashboardResponse dashboard = doctorDashboardService.getDoctorDashboard(doctorId, queryDate);

        return SuccessEntityResponse.ok("Lấy thông tin dashboard thành công", dashboard);
    }
}
