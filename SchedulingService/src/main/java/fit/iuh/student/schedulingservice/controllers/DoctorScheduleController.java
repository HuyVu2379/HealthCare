package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.responses.BulkCreateDoctorScheduleResponse;
import fit.iuh.student.schedulingservice.dtos.responses.MessageResponse;
import fit.iuh.student.schedulingservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.schedulingservice.services.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {
    private final DoctorScheduleService doctorScheduleService;
    @PostMapping("/bulk-create")
    public ResponseEntity<MessageResponse<BulkCreateDoctorScheduleResponse>> bulkCreateDoctorSchedule(
            @RequestBody BulkCreateDoctorScheduleRequest request
    ) {
        return SuccessEntityResponse.created("Bulk doctor schedules created successfully", doctorScheduleService.bulkCreateDoctorSchedule(request));
    }
}
