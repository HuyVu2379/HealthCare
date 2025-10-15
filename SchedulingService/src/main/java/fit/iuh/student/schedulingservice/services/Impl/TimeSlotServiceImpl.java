package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.dtos.requests.TimeSlotRequest;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotDTO;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotResponse;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.mappers.TimeSlotMapper;
import fit.iuh.student.schedulingservice.repositories.TimeSlotRepository;
import fit.iuh.student.schedulingservice.services.TimeSlotService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotRepository timeSlotRepository;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public TimeSlotResponse importTimeSlots(TimeSlotRequest request) {
        // Validate input
        validateTimeSlots(request.getTimeSlots());

        // Convert DTOs to entities
        List<TimeSlot> timeSlots = request.getTimeSlots().stream()
                .map(timeSlotMapper::toEntity)
                .collect(Collectors.toList());

        // Bulk save
        List<TimeSlot> savedTimeSlots = timeSlotRepository.saveAll(timeSlots);

        // Build response
        return TimeSlotResponse.builder()
                .message("Successfully imported " + savedTimeSlots.size() + " time slots")
                .totalImported(savedTimeSlots.size())
                .timeSlots(savedTimeSlots.stream()
                        .map(this::convertToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    @Override
    public List<TimeSlotDTO> getTimeSlots() {
        List<TimeSlot> timeSlots = timeSlotRepository.findAll();
        return timeSlots.stream()
                .map(this::convertToTimeSlotDTO)
                .collect(Collectors.toList());
    }

    private void validateTimeSlots(List<TimeSlotRequest.TimeSlotDto> timeSlots) {
        for (TimeSlotRequest.TimeSlotDto slot : timeSlots) {
            // Validate end time is after start time
            if (!slot.getEndTime().isAfter(slot.getStartTime())) {
                throw new IllegalArgumentException(
                        "End time must be after start time for slot: " + slot.getStartTime()
                );
            }

            // Validate slot duration (optional)
            long minutes = Duration.between(slot.getStartTime(), slot.getEndTime()).toMinutes();
            if (minutes < 15 || minutes > 240) {
                throw new IllegalArgumentException(
                        "Slot duration must be between 15 minutes and 4 hours"
                );
            }
        }
        // Check for overlapping slots
        checkForOverlaps(timeSlots);
    }

    private void checkForOverlaps(List<TimeSlotRequest.TimeSlotDto> timeSlots) {
        for (int i = 0; i < timeSlots.size(); i++) {
            for (int j = i + 1; j < timeSlots.size(); j++) {
                TimeSlotRequest.TimeSlotDto slot1 = timeSlots.get(i);
                TimeSlotRequest.TimeSlotDto slot2 = timeSlots.get(j);

                if (isOverlapping(slot1, slot2)) {
                    throw new IllegalArgumentException(
                            String.format("Time slots overlap: [%s-%s] and [%s-%s]",
                                    slot1.getStartTime(), slot1.getEndTime(),
                                    slot2.getStartTime(), slot2.getEndTime())
                    );
                }
            }
        }
    }

    private boolean isOverlapping(TimeSlotRequest.TimeSlotDto slot1,
                                  TimeSlotRequest.TimeSlotDto slot2) {
        return slot1.getStartTime().isBefore(slot2.getEndTime()) &&
                slot2.getStartTime().isBefore(slot1.getEndTime());
    }

    private TimeSlotRequest.TimeSlotDto convertToDto(TimeSlot timeSlot) {
        return timeSlotMapper.toDto(timeSlot);
    }

    private TimeSlotDTO convertToTimeSlotDTO(TimeSlot timeSlot) {
        return timeSlotMapper.toTimeSlotDTO(timeSlot);
    }
}
