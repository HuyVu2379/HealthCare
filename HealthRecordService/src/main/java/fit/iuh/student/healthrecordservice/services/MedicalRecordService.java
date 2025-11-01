package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordDetailResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordListResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordTimelineResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalRecordFullTimelineResponse;

public interface MedicalRecordService {
    CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request);
    CreateMedicalRecordResponse findByAppointmentId(String appointmentId);
    MedicalRecordListResponse getMedicalRecordsByPatientId(String patientId, int page, int size, String sortBy, String order);
    MedicalRecordDetailResponse getMedicalRecordById(String recordId);

    // ========== NEW METHODS FOR FOLLOW-UP SYSTEM ==========
    MedicalRecordTimelineResponse getMedicalRecordTimeline(String recordId);
    MedicalRecordListResponse getPatientEpisodes(String patientId, int page, int size, String sortBy, String order);

    // ========== NEW METHOD FOR FULL TIMELINE WITH EPISODES ==========
    MedicalRecordFullTimelineResponse getFullTimelineWithEpisodes(String recordId);
}
