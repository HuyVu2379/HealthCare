package fit.iuh.student.notificationservice.consumers.payload;

import lombok.Data;

@Data
public class DoctorClientResponse {
    private String doctorId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String specialty;
    private int experienceYears;
    private String avatarUrl;
    private String clinicAddress;
}