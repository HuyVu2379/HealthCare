package fit.iuh.student.healthrecordservice.dtos.responses;

import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import fit.iuh.student.healthrecordservice.enums.Frequency;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponse {
    private String prescriptionId;

    private String medicalName;

    private String dosage;

    private List<Frequency> frequency;

    private String notes;

    private String duration;
}
