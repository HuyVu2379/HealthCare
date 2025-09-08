package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;

public interface PrescriptionService {
    PrescriptionResponse createPrescription(CreatePrescriptionRequest request);
}
