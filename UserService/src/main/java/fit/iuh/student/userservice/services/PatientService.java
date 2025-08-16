package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.MedicalHistoryRequest;
import fit.iuh.student.userservice.entities.MedicalHistory;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PatientService {
    Page<MedicalHistory> getMedicalHistoriesByPatientId(String patientId, int page, int size,String sortBy, String sortDir);
    MedicalHistory updateMedicalHistories(MedicalHistoryRequest request);
}
