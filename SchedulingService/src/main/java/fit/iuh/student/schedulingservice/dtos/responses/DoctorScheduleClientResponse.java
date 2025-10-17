package fit.iuh.student.schedulingservice.dtos.responses;

import lombok.Builder;
import lombok.Data;
@Data
@Builder
public class DoctorScheduleClientResponse {
    private String doctorId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String specialty;
    private int experienceYears;
    private String avatarUrl;
    private String clinicAddress;
    private String scheduleId;
}
