package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.CreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateDoctorSchedule;
import fit.iuh.student.schedulingservice.dtos.responses.*;
import fit.iuh.student.schedulingservice.exceptions.errors.BadRequestException;
import fit.iuh.student.schedulingservice.repositories.DoctorScheduleRepository;
import fit.iuh.student.schedulingservice.services.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/doctor-schedules")
@RequiredArgsConstructor
public class DoctorScheduleController {
    private final DoctorScheduleService doctorScheduleService;
    private final DoctorScheduleRepository doctorScheduleRepository;
    @PostMapping("/create")
    public ResponseEntity<MessageResponse<DoctorScheduleResponse>> CreateDoctorSchedule(
            @RequestBody CreateDoctorScheduleRequest request
    ) {
        return SuccessEntityResponse.created("doctor schedules created successfully", doctorScheduleService.createDoctorSchedule(request));
    }
    @PostMapping("/bulk-create")
    public ResponseEntity<MessageResponse<BulkCreateDoctorScheduleResponse>> bulkCreateDoctorSchedule(
            @RequestBody BulkCreateScheduleRequest request
    ) {
        return SuccessEntityResponse.created("Doctor schedules created successfully", doctorScheduleService.bulkCreateDoctorSchedule(request));
    }

    @DeleteMapping("/remove-timeSlots/{scheduleId}")
    public ResponseEntity<MessageResponse<Boolean>> updateTimeSlots(
            @RequestBody UpdateDoctorSchedule request
            ) {
        return SuccessEntityResponse.ok("Time slots updated successfully", doctorScheduleService.updateDoctorSchedule(request));
    }
    @GetMapping("/getDoctorScheduleByDoctorIdAndDate")
    public ResponseEntity<MessageResponse<DoctorScheduleResponse>> getDoctorScheduleByDoctorIdAndDate(
            @RequestParam String doctorId,
            @RequestParam String date
    ) {
        Date date1 = Date.valueOf(date);
        DoctorScheduleResponse doctorScheduleResponse = doctorScheduleService.getDoctorScheduleByDate(doctorId, date1);
        return SuccessEntityResponse.ok("Get doctor schedule by doctor id and date successfully", doctorScheduleResponse);
    }
    @GetMapping("/getDoctorOfDate")
    public ResponseEntity<MessageResponse<List<String>>> getDoctorOfDate(
            @RequestParam String date
    ) {
        Date date1 = Date.valueOf(date);
        if(date1.before(Date.valueOf(LocalDate.now().minusDays(1)))) {
            throw new BadRequestException("Date must be today or later");
        }
        List<String> doctorIds = doctorScheduleRepository.findDoctorIdsByDate(date1);
        return SuccessEntityResponse.ok("Get doctor ids by date successfully", doctorIds);
    }

    @GetMapping("/getDoctorByDateAndTimeSlot")
    public ResponseEntity<MessageResponse<List<DoctorScheduleClientResponse>>> getDoctorByDateAndTimeSlot(
            @RequestParam String date,
            @RequestParam int slotId
    ) {
        Date date1 = Date.valueOf(date);
        if(date1.before(Date.valueOf(LocalDate.now().minusDays(1)))) {
            throw new BadRequestException("Date must be today or later");
        }
        List<DoctorScheduleClientResponse> doctors = doctorScheduleService.getDoctorByDateAndTimeSlot(date1, slotId);
        return SuccessEntityResponse.ok("Get doctors by date and time slot successfully", doctors);
    }
}
