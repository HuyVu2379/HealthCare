package fit.iuh.student.healthrecordservice.dtos.requests;

import fit.iuh.student.healthrecordservice.enums.Frequency;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePrescriptionRequest {

    private String medicalRecordId;

    private String medicalName;

    private String dosage;

    private List<Frequency> frequency;

    private String notes;

    private Date startDate;

    private Date endDate;
}
