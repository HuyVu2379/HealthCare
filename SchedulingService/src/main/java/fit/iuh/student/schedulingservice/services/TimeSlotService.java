package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotResponse;

public interface TimeSlotService {
    TimeSlotResponse importTimeSlots(TimeSlotRequest timeSlotRequest);
}
