package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;

public interface MedicalRecordService {
    CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request);
}
