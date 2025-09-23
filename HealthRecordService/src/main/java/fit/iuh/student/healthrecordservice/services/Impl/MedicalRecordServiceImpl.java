package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.clients.UserClient;
import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;
import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import fit.iuh.student.healthrecordservice.exceptions.errors.DuplicationObjectException;
import fit.iuh.student.healthrecordservice.publishers.MedicalRecordEventPublisher;
import fit.iuh.student.healthrecordservice.publishers.payload.MedicalRecordPayload;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.services.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.Date;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordEventPublisher medicalRecordEventPublisher;
    private final UserClient userClient;
    @Override
    public CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request) {
        try{
            boolean isExist = medicalRecordRepository.existsAppointmentId(request.getAppointmentId());
            MedicalRecord medicalRecord;
            
            if(isExist){
                // Cập nhật record hiện có
                medicalRecord = medicalRecordRepository.findByAppointmentId(request.getAppointmentId());
                medicalRecord.setDiagnosis(request.getDiagnosis());
                medicalRecord.setDoctorNote(request.getDoctorNote());
                medicalRecord.setFollowUpDate(
                        (request.getFollowUpDate() == null || request.getFollowUpDate().trim().isEmpty())
                                ? null
                                : Date.valueOf(request.getFollowUpDate())
                );
                medicalRecord.setImageAttachments(request.getImageAttachments());
                medicalRecord.setSymptoms(request.getSymptoms());
                medicalRecord.setTreatment(request.getTreatment());
                medicalRecord.setServiceName(request.getServiceName());
            } else {
                // Tạo record mới
                medicalRecord = MedicalRecord.builder()
                        .appointmentId(request.getAppointmentId())
                        .patientId(request.getPatientId())
                        .doctorId(request.getDoctorId())
                        .diagnosis(request.getDiagnosis())
                        .doctorNote(request.getDoctorNote())
                        .followUpDate(
                                (request.getFollowUpDate() == null || request.getFollowUpDate().trim().isEmpty())
                                        ? null
                                        : Date.valueOf(request.getFollowUpDate())
                        )
                        .healthMetrics(null)
                        .prescriptions(null)
                        .imageAttachments(request.getImageAttachments())
                        .symptoms(request.getSymptoms())
                        .treatment(request.getTreatment())
                        .serviceName(request.getServiceName())
                        .build();
            }
            medicalRecord = medicalRecordRepository.save(medicalRecord);
            
            // Publish event - wrap in try-catch to ensure API success even if event fails
            try {
                medicalRecordEventPublisher.publishCreateMedicalRecordEvent(
                        MedicalRecordPayload.builder()
                                .appointmentId(medicalRecord.getAppointmentId())
                                .serviceName(medicalRecord.getServiceName())
                                .diagnosis(medicalRecord.getDiagnosis())
                                .doctorNote(medicalRecord.getDoctorNote())
                                .dateDiagnosis(Date.valueOf(medicalRecord.getCreatedAt().toLocalDate()))
                                .stage(request.getStage())
                                .symptoms(medicalRecord.getSymptoms())
                                .treatment(medicalRecord.getTreatment())
                                .statusHealth(request.getStatusHealth())
                                .eventType("MEDICAL_RECORD_CREATED")
                                .patient(userClient.getPatientForClient(request.getPatientId()))
                                .build());
            } catch (Exception eventException) {
                // Log event error but don't fail the API response
                System.err.println("Failed to publish medical record event: " + eventException.getMessage());
                // Continue with success response since record was saved successfully
            }
            
            return CreateMedicalRecordResponse.builder()
                    .recordId(medicalRecord.getRecordId())
                    .patientId(request.getPatientId())
                    .doctorId(request.getDoctorId())
                    .appointmentId(medicalRecord.getAppointmentId())
                    .diagnosis(medicalRecord.getDiagnosis())
                    .doctorNote(medicalRecord.getDoctorNote())
                    .followUpDate(medicalRecord.getFollowUpDate())
                    .imageAttachments(medicalRecord.getImageAttachments())
                    .symptoms(medicalRecord.getSymptoms())
                    .treatment(medicalRecord.getTreatment())
                    .serviceName(medicalRecord.getServiceName())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public CreateMedicalRecordResponse findByAppointmentId(String appointmentId) {
        try {
            MedicalRecord medicalRecord = medicalRecordRepository.findByAppointmentId(appointmentId);
            if (medicalRecord == null) {
                return null;
            }
            
            return CreateMedicalRecordResponse.builder()
                    .recordId(medicalRecord.getRecordId())
                    .patientId(medicalRecord.getPatientId())
                    .doctorId(medicalRecord.getDoctorId())
                    .appointmentId(medicalRecord.getAppointmentId())
                    .diagnosis(medicalRecord.getDiagnosis())
                    .doctorNote(medicalRecord.getDoctorNote())
                    .followUpDate(medicalRecord.getFollowUpDate())
                    .imageAttachments(medicalRecord.getImageAttachments())
                    .symptoms(medicalRecord.getSymptoms())
                    .treatment(medicalRecord.getTreatment())
                    .serviceName(medicalRecord.getServiceName())
                    .build();
        } catch (Exception e) {
            System.err.println("Error finding medical record by appointmentId: " + e.getMessage());
            return null;
        }
    }
}
