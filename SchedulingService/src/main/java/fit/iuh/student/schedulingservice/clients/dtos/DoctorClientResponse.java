package fit.iuh.student.schedulingservice.clients.dtos;

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
    private Double rating;
    private Integer examinationFee;
}
