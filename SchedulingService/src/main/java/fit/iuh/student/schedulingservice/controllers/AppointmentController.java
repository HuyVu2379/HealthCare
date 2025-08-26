package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.MessageResponse;
import fit.iuh.student.schedulingservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    private final AppointmentService appointmentService;

    @PostMapping("/booking-appointment")
    public ResponseEntity<MessageResponse<AppointmentResponse>> bookingAppointment(
            @RequestBody CreateAppointmentRequest request
            ) {
        return SuccessEntityResponse.created("Booking appointment successfully",
                appointmentService.bookingAppointment(request));
    }
    @DeleteMapping("/{appointmentId}/cancel")
    public ResponseEntity<MessageResponse<Boolean>> cancelAppointment(
            @PathVariable String appointmentId,
            @RequestParam String userId
    ) {
        return SuccessEntityResponse.ok("Cancel appointment successfully",
                appointmentService.cancelAppointment(appointmentId, userId));
    }

    @GetMapping("/get-appointment-with-patientId")
    public ResponseEntity<MessageResponse<Page<AppointmentResponse>>> getAppointmentByPatientId(
            @RequestParam String patientId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value ="startTime") String startTime,
            @RequestParam(value ="endTime") String endTime,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir
    ){
        return SuccessEntityResponse.ok("Get appointment by patient id successfully",
                appointmentService.getAppointmentByPatientIdWithPage(patientId, page, size, sortBy, startTime, endTime, sortDir));
    }
    @PutMapping("/reschedule-appointment")
    public ResponseEntity<MessageResponse<AppointmentResponse>> rescheduleAppointment(
            @RequestBody UpdateAppointmentRequest request
    ) {
        return SuccessEntityResponse.ok("Reschedule appointment successfully",
                appointmentService.rescheduleAppointment(request));
    }
    @PutMapping("/{appointmentId}/update-status")
    public ResponseEntity<MessageResponse<AppointmentResponse>> updateAppointmentStatus(
            @PathVariable String appointmentId,
            @RequestParam String status
    ) {
        return SuccessEntityResponse.ok("Update appointment status successfully",
                appointmentService.updateAppointmentStatus(appointmentId, Enum.valueOf(AppointmentStatus.class, status)));
    }
    @GetMapping("/{appointmentId}/detail")
    public ResponseEntity<MessageResponse<AppointmentResponse>> getAppointmentDetailById(
            @PathVariable String appointmentId
    ) {
        return SuccessEntityResponse.ok("Get appointment detail successfully",
                appointmentService.getAppointmentDetailById(appointmentId));
    }
}
