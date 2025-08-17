package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.responses.BulkCreateDoctorScheduleResponse;
import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.repositories.DoctorScheduleRepository;
import fit.iuh.student.schedulingservice.repositories.TimeSlotRepository;
import fit.iuh.student.schedulingservice.services.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorScheduleServiceImpl implements DoctorScheduleService {
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    @Override
    @Transactional
    public BulkCreateDoctorScheduleResponse bulkCreateDoctorSchedule(BulkCreateDoctorScheduleRequest request) {
        // Create a new DoctorSchedule entity from the request
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        doctorSchedule.setDoctorId(request.getDoctorId());
        doctorSchedule.setWeekDay(request.getWeekDay());
        doctorSchedule.setWorkDate(request.getWorkDate());
        doctorSchedule.setAvailable(request.isAvailable());

        // Process time slots
        List<Integer> timeSlots = request.getTimeSlotIds();
        if (timeSlots != null && !timeSlots.isEmpty()) {
            // Fetch existing time slots from database
            List<TimeSlot> existingTimeSlots = timeSlotRepository.findAllById(timeSlots);

            // Process each time slot ID
            for (Integer timeSlotId : timeSlots) {
                // Find existing time slot by ID
                TimeSlot existingTimeSlot = existingTimeSlots.stream()
                        .filter(ts -> ts.getSlotId().equals(timeSlotId))
                        .findFirst()
                        .orElse(null);

                if (existingTimeSlot != null) {
                    doctorSchedule.addTimeSlot(existingTimeSlot);
                }
            }
        }

        // Save the doctor schedule
        DoctorSchedule savedSchedule = doctorScheduleRepository.save(doctorSchedule);

        // Create and return the response
        return BulkCreateDoctorScheduleResponse.builder()
                .schedule_id(savedSchedule.getScheduleId())
                .doctorId(savedSchedule.getDoctorId())
                .weekDay(savedSchedule.getWeekDay())
                .workDate(savedSchedule.getWorkDate())
                .isAvailable(savedSchedule.isAvailable())
                .timeSlots(new ArrayList<>(savedSchedule.getTimeSlots()))
                .build();
    }
}
