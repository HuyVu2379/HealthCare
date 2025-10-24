package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.clients.AppointmentClient;
import fit.iuh.student.healthrecordservice.clients.UserClient;
import fit.iuh.student.healthrecordservice.clients.dtos.AppointmentClientResponse;
import fit.iuh.student.healthrecordservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.healthrecordservice.clients.dtos.PatientClientResponse;
import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.*;
import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import fit.iuh.student.healthrecordservice.entities.Prescription;
import fit.iuh.student.healthrecordservice.enums.EpisodeType;
import fit.iuh.student.healthrecordservice.exceptions.errors.DuplicationObjectException;
import fit.iuh.student.healthrecordservice.exceptions.errors.NotFoundException;
import fit.iuh.student.healthrecordservice.publishers.MedicalRecordEventPublisher;
import fit.iuh.student.healthrecordservice.publishers.payload.MedicalRecordPayload;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.services.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.sql.Date;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final MedicalRecordEventPublisher medicalRecordEventPublisher;
    private final UserClient userClient;
    private final AppointmentClient appointmentClient; // NEW: Inject AppointmentClient
    @Override
    public CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request) {
        try{
            boolean isExist = medicalRecordRepository.existsAppointmentId(request.getAppointmentId());
            MedicalRecord medicalRecord;

            // ========== NEW: Get appointment info to check if it's FOLLOW_UP ==========
            AppointmentClientResponse appointmentInfo = null;
            try {
                appointmentInfo = appointmentClient.getAppointmentForClient(request.getAppointmentId());
            } catch (Exception e) {
                System.err.println("Failed to get appointment info: " + e.getMessage());
            }

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
                medicalRecord.setSignatureUrl(request.getSignatureUrl());
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
                        .signatureUrl(request.getSignatureUrl())
                        .build();

                // ========== AUTO-DETECT EPISODE TYPE ==========
                if (appointmentInfo != null && "FOLLOW_UP".equals(appointmentInfo.getConsultationType())) {
                    medicalRecord.setEpisodeType(EpisodeType.FOLLOW_UP);
                    medicalRecord.setParentRecordId(appointmentInfo.getRelatedRecordId());
                } else {
                    medicalRecord.setEpisodeType(EpisodeType.INITIAL);
                    medicalRecord.setParentRecordId(null);
                }
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
                                .signatureUrl(medicalRecord.getSignatureUrl())
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
                    .signatureUrl(medicalRecord.getSignatureUrl())
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
                    .signatureUrl(medicalRecord.getSignatureUrl())
                    .build();
        } catch (Exception e) {
            System.err.println("Error finding medical record by appointmentId: " + e.getMessage());
            return null;
        }
    }

    @Override
    public MedicalRecordListResponse getMedicalRecordsByPatientId(String patientId, int page, int size, String sortBy, String order) {
        try {
            // Create sorting
            Sort sort = order.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Query medical records
            Page<MedicalRecord> medicalRecordPage = medicalRecordRepository.findByPatientId(patientId, pageable);

            // Convert to response DTOs
            List<MedicalRecordDetailResponse> records = medicalRecordPage.getContent().stream()
                    .map(this::convertToDetailResponse)
                    .collect(Collectors.toList());

            // Create pagination response
            PaginationResponse pagination = PaginationResponse.builder()
                    .currentPage(medicalRecordPage.getNumber())
                    .totalPages(medicalRecordPage.getTotalPages())
                    .totalRecords(medicalRecordPage.getTotalElements())
                    .pageSize(medicalRecordPage.getSize())
                    .build();

            return MedicalRecordListResponse.builder()
                    .records(records)
                    .pagination(pagination)
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Error getting medical records by patient ID: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicalRecordDetailResponse getMedicalRecordById(String recordId) {
        try {
            MedicalRecord medicalRecord = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new NotFoundException("Medical record not found with ID: " + recordId));

            return convertToDetailResponse(medicalRecord);
        } catch (Exception e) {
            throw new RuntimeException("Error getting medical record by ID: " + e.getMessage(), e);
        }
    }

    private MedicalRecordDetailResponse convertToDetailResponse(MedicalRecord medicalRecord) {
        // Get doctor name from UserClient
        String doctorName = null;
        try {
            DoctorClientResponse doctor = userClient.getDoctorForClient(medicalRecord.getDoctorId());
            doctorName = doctor != null ? doctor.getFullName() : null;
        } catch (Exception e) {
            System.err.println("Error fetching doctor info: " + e.getMessage());
        }

        // Get patient info from UserClient
        PatientClientResponse patient = null;
        try {
            patient = userClient.getPatientForClient(medicalRecord.getPatientId());
        } catch (Exception e) {
            System.err.println("Error fetching patient info: " + e.getMessage());
        }

        // Convert prescriptions
        List<PrescriptionResponse> prescriptions = medicalRecord.getPrescriptions().stream()
                .map(this::convertToPrescriptionResponse)
                .collect(Collectors.toList());

        return MedicalRecordDetailResponse.builder()
                .recordId(medicalRecord.getRecordId())
                .appointmentId(medicalRecord.getAppointmentId())
                .patientId(medicalRecord.getPatientId())
                .doctorId(medicalRecord.getDoctorId())
                .doctorName(doctorName)
                .patient(patient)
                .serviceName(medicalRecord.getServiceName())
                .diagnosis(medicalRecord.getDiagnosis())
                .symptoms(medicalRecord.getSymptoms())
                .treatment(medicalRecord.getTreatment())
                .doctorNote(medicalRecord.getDoctorNote())
                .followUpDate(medicalRecord.getFollowUpDate())
                .imageAttachments(medicalRecord.getImageAttachments())
                .signatureUrl(medicalRecord.getSignatureUrl())
                .stage(null)
                .statusHealth(null)
                .createdAt(medicalRecord.getCreatedAt() != null ?
                        java.util.Date.from(medicalRecord.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant()) : null)
                .updatedAt(medicalRecord.getUpdatedAt() != null ?
                        java.util.Date.from(medicalRecord.getUpdatedAt().atZone(ZoneId.systemDefault()).toInstant()) : null)
                .prescriptions(prescriptions)
                // ========== NEW FIELDS ==========
                .parentRecordId(medicalRecord.getParentRecordId())
                .episodeType(medicalRecord.getEpisodeType() != null ? medicalRecord.getEpisodeType().name() : null)
                .build();
    }

    private PrescriptionResponse convertToPrescriptionResponse(Prescription prescription) {
        long durationInDays = 0;
        if (prescription.getEndDate() != null && prescription.getStartDate() != null) {
            long diffInMillies = prescription.getEndDate().getTime() - prescription.getStartDate().getTime();
            durationInDays = diffInMillies / (24 * 60 * 60 * 1000);
        }

        return PrescriptionResponse.builder()
                .prescriptionId(prescription.getPrescriptionId())
                .medicalName(prescription.getMedicalName())
                .dosage(prescription.getDosage())
                .frequency(prescription.getFrequency())
                .notes(prescription.getNotes())
                .duration(durationInDays + " ngày")
                .startDate(prescription.getStartDate())
                .endDate(prescription.getEndDate())
                .build();
    }

    // ========== NEW METHODS FOR FOLLOW-UP SYSTEM ==========

    @Override
    public MedicalRecordTimelineResponse getMedicalRecordTimeline(String recordId) {
        try {
            // 1. Get the record
            MedicalRecord record = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new NotFoundException("Medical record not found"));

            // 2. Find root record (trace back if this is a follow-up)
            MedicalRecord rootRecord = record;
            while (rootRecord.getParentRecordId() != null) {
                rootRecord = medicalRecordRepository.findById(rootRecord.getParentRecordId())
                        .orElseThrow(() -> new NotFoundException("Parent record not found"));
            }

            // 3. Get all follow-ups of root record
            List<MedicalRecord> followUps = medicalRecordRepository.findFollowUpRecords(rootRecord.getRecordId());

            // 4. Convert to response
            return MedicalRecordTimelineResponse.builder()
                    .rootRecord(convertToDetailResponse(rootRecord))
                    .followUpRecords(followUps.stream()
                            .map(this::convertToDetailResponse)
                            .collect(Collectors.toList()))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error getting timeline: " + e.getMessage(), e);
        }
    }

    @Override
    public MedicalRecordListResponse getPatientEpisodes(String patientId, int page, int size, String sortBy, String order) {
        try {
            Sort sort = order.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
            Pageable pageable = PageRequest.of(page, size, sort);

            // Get only INITIAL records (not follow-ups)
            Page<MedicalRecord> recordPage = medicalRecordRepository.findInitialRecordsByPatientId(patientId, pageable);

            // Convert to response
            List<MedicalRecordDetailResponse> records = recordPage.getContent().stream()
                    .map(this::convertToDetailResponse)
                    .collect(Collectors.toList());

            PaginationResponse pagination = PaginationResponse.builder()
                    .currentPage(recordPage.getNumber())
                    .totalPages(recordPage.getTotalPages())
                    .totalRecords(recordPage.getTotalElements())
                    .pageSize(recordPage.getSize())
                    .build();

            return MedicalRecordListResponse.builder()
                    .records(records)
                    .pagination(pagination)
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Error getting episodes: " + e.getMessage(), e);
        }
    }
}
