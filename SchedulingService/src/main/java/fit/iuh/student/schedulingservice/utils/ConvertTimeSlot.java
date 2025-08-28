package fit.iuh.student.schedulingservice.utils;

import fit.iuh.student.schedulingservice.entities.TimeSlot;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Component
public class ConvertTimeSlot {
    public Date convertTimeSlotToDate(TimeSlot timeSlot, Date appointmentDate) {
        int startHour = timeSlot.getStartTime().getHour();
        int startMinute = timeSlot.getEndTime().getHour();
        // Convert Date to LocalDate
        LocalDate localDate = appointmentDate.toLocalDate();
        // Combine LocalDate with startHour and startMinute to create LocalDateTime
        LocalDateTime localDateTime = localDate.atTime(startHour, startMinute);
        // Convert LocalDateTime back to Date
        return Date.valueOf(localDateTime.toLocalDate());
    }
}
