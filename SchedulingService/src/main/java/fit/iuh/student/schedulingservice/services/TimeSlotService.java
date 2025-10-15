package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotDTO;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotResponse;

import java.util.List;

public interface TimeSlotService {
    TimeSlotResponse importTimeSlots(TimeSlotRequest timeSlotRequest);
    List<TimeSlotDTO> getTimeSlots();
}
