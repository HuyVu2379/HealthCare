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

    /**
     * Download prescription as PDF
     *
     * @param recordId Medical record ID
     * @return PDF as byte array
     * @throws Exception if record not found, no prescriptions, or unauthorized
     */
    byte[] downloadPrescriptionPdf(String recordId) throws Exception;

    /**
     * Generate prescription PDF filename based on appointment date
     *
     * @param recordId Medical record ID
     * @return Filename in format: don-thuoc-DDMMYYYY.pdf
     */
    String generatePrescriptionFilename(String recordId);
}
