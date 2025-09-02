package fit.iuh.student.healthrecordservice.dtos.requests;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMedicalRecordRequest {
    private String appointmentId;
    private String diagnosis;
    private String treatment;
    private String symptoms;
    private Date followUpDate;
    private String doctorNote;
    private List<String> imageAttachments = new ArrayList<>();
}
