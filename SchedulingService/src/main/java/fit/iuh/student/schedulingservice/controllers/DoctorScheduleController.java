package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.CreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.responses.BulkCreateDoctorScheduleResponse;
import fit.iuh.student.schedulingservice.dtos.responses.DoctorScheduleResponse;
import fit.iuh.student.schedulingservice.dtos.responses.MessageResponse;
import fit.iuh.student.schedulingservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.exceptions.errors.BadRequestException;
import fit.iuh.student.schedulingservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.schedulingservice.repositories.DoctorScheduleRepository;
import fit.iuh.student.schedulingservice.repositories.TimeSlotRepository;
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
    private final TimeSlotRepository timeSlotRepository;
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
            @PathVariable String scheduleId,
            @RequestParam("timeSlotIds") Integer[] timeSlotIds
    ) {
        DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new UserNotFoundException("Doctor schedule not found"));
        List<TimeSlot> timeSlotList = timeSlotRepository.findAllById(List.of(timeSlotIds));
        for (TimeSlot timeSlot : timeSlotList) {
            doctorSchedule.removeTimeSlot(timeSlot);
        }
        doctorScheduleRepository.save(doctorSchedule);
        return SuccessEntityResponse.ok("Time slots updated successfully", true);
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
}
