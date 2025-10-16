package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionGroupResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;

import java.util.List;

public interface PrescriptionService {
    PrescriptionResponse createPrescription(CreatePrescriptionRequest request);
    List<PrescriptionResponse> getPrescriptionUsing(String patientId);
    List<PrescriptionResponse> getPrescriptionsByMedicalRecordId(String medicalRecordId);
    List<PrescriptionGroupResponse> getPrescriptionGroups(String patientId);
}
