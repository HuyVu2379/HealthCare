package fit.iuh.student.schedulingservice.clients.dtos;

import lombok.Data;
@Data
public class PatientClientResponse {
    private String userId;
    private String email;
    private String fullName;
    private String phone;
    private String avatarUrl;
}
