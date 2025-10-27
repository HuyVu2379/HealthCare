package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.ScheduleFollowUpByDoctorRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.*;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @PostMapping("/schedule-follow-up-by-doctor")
    public ResponseEntity<MessageResponse<AppointmentResponse>> scheduleFollowUpByDoctor(
            @Valid @RequestBody ScheduleFollowUpByDoctorRequest request
    ) {
        try {
            AppointmentResponse response = appointmentService.scheduleFollowUpByDoctor(request);
            return SuccessEntityResponse.created("Đặt lịch tái khám thành công", response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new MessageResponse<>(500, "Không thể đặt lịch tái khám: " + e.getMessage(), false, null)
            );
        }
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
            @RequestParam(value = "startTime") String startTime,
            @RequestParam(value = "endTime") String endTime,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir
    ) {
        return SuccessEntityResponse.ok("Get appointment by patient id successfully",
                appointmentService.getAppointmentByPatientIdWithPage(patientId, page, size, sortBy, startTime, endTime, sortDir));
    }

    @PutMapping("/reschedule-appointment")
    public ResponseEntity<MessageResponse<RescheduleAppointmentResponse>> rescheduleAppointment(
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

    @GetMapping("/filter")
    public ResponseEntity<MessageResponse<Page<AppointmentResponse>>> getAppointmentFilterWithPagination(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "DESC") String sortDir
    ) {
        AppointmentStatus statusEnum = null;
        if (status != null && !status.isEmpty()) {
            statusEnum = AppointmentStatus.valueOf(status);
        }
        return SuccessEntityResponse.ok("Get appointment with filter successfully",
                appointmentService.getAppointmentWithFilterPagination(type, statusEnum, page, size, sortBy, sortDir));
    }

    @GetMapping("/get-appointment-with-doctorId")
    public ResponseEntity<MessageResponse<List<AppointmentWeekFilterResponse>>> getAppointmentByDoctorId(
            @RequestParam String doctorId,
            @RequestParam(value = "startTime") String startTime,
            @RequestParam(value = "endTime") String endTime
    ) {
        return SuccessEntityResponse.ok("Get appointment by doctor id successfully",
                appointmentService.getAppointmentWeekFilterForDoctor(doctorId, startTime, endTime));
    }

    @GetMapping("/client/{appointmentId}/detail")
    public AppointmentClientResponse getAppointmentDetailForClientById(
            @PathVariable String appointmentId
    ) {
        return appointmentService.getAppointmentDetailForClientById(appointmentId);
    }

    @GetMapping("/get-appointment-for-patient-with-filter")
    public ResponseEntity<MessageResponse<Page<AppointmentResponse>>> getAppointmentByPatientIdWithFilter(
            @RequestParam String patientId,
            @RequestParam String consultationType,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam(required = false) String startTime,
            @RequestParam(required = false) String endTime
    ){
        return SuccessEntityResponse.ok("Get appointment by patient id with filter successfully",
                appointmentService.getAppointmentWithFilterPaginationForPatient(patientId, Enum.valueOf(ConsultationType.class, consultationType), page, size, startTime, endTime));
    }
}
