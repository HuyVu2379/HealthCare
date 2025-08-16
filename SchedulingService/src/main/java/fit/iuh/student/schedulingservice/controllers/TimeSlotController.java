package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest;
import fit.iuh.student.schedulingservice.dtos.responses.MessageResponse;
import fit.iuh.student.schedulingservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotResponse;
import fit.iuh.student.schedulingservice.services.TimeSlotService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/time-slots")
@RequiredArgsConstructor
public class TimeSlotController {
    private final TimeSlotService timeSlotService;
    @PostMapping("/import")
    public ResponseEntity<MessageResponse<TimeSlotResponse>> importTimeSlots(@Valid @RequestBody TimeSlotRequest request) {
        TimeSlotResponse response = timeSlotService.importTimeSlots(request);
        return SuccessEntityResponse.created("Time slots imported successfully", response);
    }
}
