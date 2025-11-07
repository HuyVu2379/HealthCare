package fit.iuh.student.userservice.dtos.responses;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DoctorClientResponse {
    private String doctorId;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String specialty;
    private int experienceYears;
    private String avatarUrl;
    private String clinicAddress;
    private int examinationFee;
    private double rating;
    private String bio;
}
