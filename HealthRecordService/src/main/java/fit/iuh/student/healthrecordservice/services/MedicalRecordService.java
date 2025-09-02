package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;
import fit.iuh.student.healthrecordservice.entities.MedicalRecord;

public interface MedicalRecordService {
    CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request);
}
