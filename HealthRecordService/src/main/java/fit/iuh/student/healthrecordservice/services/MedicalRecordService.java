package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordDetailResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordListResponse;

public interface MedicalRecordService {
    CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request);
    CreateMedicalRecordResponse findByAppointmentId(String appointmentId);
    MedicalRecordListResponse getMedicalRecordsByPatientId(String patientId, int page, int size, String sortBy, String order);
    MedicalRecordDetailResponse getMedicalRecordById(String recordId);
}
