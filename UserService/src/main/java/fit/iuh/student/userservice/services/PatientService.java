package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.MedicalHistoryRequest;
import fit.iuh.student.userservice.dtos.requests.UpdatePatientRequest;
import fit.iuh.student.userservice.dtos.responses.DoctorClientResponse;
import fit.iuh.student.userservice.dtos.responses.PatientClientResponse;
import fit.iuh.student.userservice.dtos.responses.PatientResponse;
import fit.iuh.student.userservice.dtos.responses.UpdatePatientResponse;
import fit.iuh.student.userservice.entities.MedicalHistory;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PatientService {
    Page<MedicalHistory> getMedicalHistoriesByPatientId(String patientId, int page, int size,String sortBy, String sortDir);
    MedicalHistory updateMedicalHistories(MedicalHistoryRequest request);
    UpdatePatientResponse updatePatient(UpdatePatientRequest request);
    PatientClientResponse getPatientByIdForClient(String patientId);
    Page<PatientResponse> getPatientsByDoctorId(String doctorId, int page, int size, String sortBy, String sortDir, String namePatient,String statusHealth);
}
