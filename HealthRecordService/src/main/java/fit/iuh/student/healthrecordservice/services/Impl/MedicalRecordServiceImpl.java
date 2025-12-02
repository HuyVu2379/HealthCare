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
import java.util.*;
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
                System.out.println("✅ Successfully retrieved appointment info - ID: " + request.getAppointmentId() +
                                   ", ConsultationType: " + (appointmentInfo != null ? appointmentInfo.getConsultationType() : "null") +
                                   ", RelatedRecordId: " + (appointmentInfo != null ? appointmentInfo.getRelatedRecordId() : "null"));
            } catch (Exception e) {
                System.err.println("❌ CRITICAL ERROR: Failed to get appointment info for appointmentId: " + request.getAppointmentId());
                System.err.println("Error Type: " + e.getClass().getName());
                System.err.println("Error Message: " + e.getMessage());
                e.printStackTrace();

                // FAIL FAST: Không tạo medical record với episode type sai
                throw new RuntimeException("Cannot create medical record: Failed to retrieve appointment details. " +
                        "This is required to determine if this is an initial visit or follow-up. " +
                        "Error: " + e.getMessage(), e);
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

        // Get appointment date from AppointmentClient
        AppointmentClientResponse appointment = null;
        try {
            appointment = appointmentClient.getAppointmentForClient(medicalRecord.getAppointmentId());
        } catch (Exception e) {
            System.err.println("Error fetching appointment info: " + e.getMessage());
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
                .appointmentDate(appointment != null ? appointment.getAppointmentDate() : null)
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

    // ========== NEW METHOD: FULL TIMELINE WITH EPISODES ==========

    @Override
    public MedicalRecordFullTimelineResponse getFullTimelineWithEpisodes(String recordId) {
        try {
            // 1. Get current record
            MedicalRecord currentRecord = medicalRecordRepository.findById(recordId)
                    .orElseThrow(() -> new NotFoundException("Medical record not found"));

            String patientId = currentRecord.getPatientId();
            String doctorId = currentRecord.getDoctorId();

            System.out.println("📊 Getting full timeline for recordId: " + recordId);
            System.out.println("📊 Patient: " + patientId + ", Doctor: " + doctorId);

            // 2. Get ALL records of patient with this doctor
            List<MedicalRecord> allRecords = medicalRecordRepository
                    .findByPatientIdAndDoctorIdOrderByCreatedAtDesc(patientId, doctorId);

            System.out.println("📊 Found " + allRecords.size() + " total records for patient with doctor");

            // 3. Group records by episode (root record)
            Map<String, List<MedicalRecord>> episodeMap = groupRecordsByEpisode(allRecords);

            System.out.println("📂 Grouped into " + episodeMap.size() + " episodes");

            // 4. Convert to EpisodeGroup list
            List<MedicalRecordFullTimelineResponse.EpisodeGroup> episodes = episodeMap.entrySet().stream()
                    .map(entry -> buildEpisodeGroup(entry.getKey(), entry.getValue(), recordId))
                    .sorted((a, b) -> {
                        // Current episode first
                        if (a.getIsCurrentEpisode() && !b.getIsCurrentEpisode()) return -1;
                        if (!a.getIsCurrentEpisode() && b.getIsCurrentEpisode()) return 1;
                        // Then by date DESC
                        return b.getFirstVisitDate().compareTo(a.getFirstVisitDate());
                    })
                    .collect(Collectors.toList());

            // 5. Build response
            return MedicalRecordFullTimelineResponse.builder()
                    .totalVisits((long) allRecords.size())
                    .totalEpisodes(episodeMap.size())
                    .episodes(episodes)
                    .build();

        } catch (Exception e) {
            System.err.println("❌ Error getting full timeline: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error getting full timeline: " + e.getMessage(), e);
        }
    }

    /**
     * Group records by their root record (episode)
     */
    private Map<String, List<MedicalRecord>> groupRecordsByEpisode(List<MedicalRecord> allRecords) {
        Map<String, List<MedicalRecord>> episodeMap = new HashMap<>();

        for (MedicalRecord record : allRecords) {
            String rootId = findRootRecordId(record);
            episodeMap.computeIfAbsent(rootId, k -> new ArrayList<>()).add(record);
        }

        return episodeMap;
    }

    /**
     * Find root record ID by tracing back parent_record_id chain
     */
    private String findRootRecordId(MedicalRecord record) {
        MedicalRecord current = record;
        int maxDepth = 10; // Prevent infinite loop
        int depth = 0;

        while (current.getParentRecordId() != null && depth < maxDepth) {
            try {
                current = medicalRecordRepository.findById(current.getParentRecordId())
                        .orElse(current); // If parent not found, use current as root
            } catch (Exception e) {
                System.err.println("⚠️ Error finding parent record: " + e.getMessage());
                break;
            }
            depth++;
        }

        return current.getRecordId();
    }

    /**
     * Build EpisodeGroup from a list of records in the same episode
     */
    private MedicalRecordFullTimelineResponse.EpisodeGroup buildEpisodeGroup(
            String rootId,
            List<MedicalRecord> records,
            String currentRecordId
    ) {
        // Sort by date DESC (newest first)
        records.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        // Check if this episode contains current visit
        boolean isCurrentEpisode = records.stream()
                .anyMatch(r -> r.getRecordId().equals(currentRecordId));

        // Get root record (INITIAL visit)
        MedicalRecord rootRecord = records.stream()
                .filter(r -> r.getEpisodeType() == EpisodeType.INITIAL)
                .findFirst()
                .orElse(records.get(records.size() - 1)); // Fallback to oldest

        // Build visit details
        List<MedicalRecordFullTimelineResponse.VisitDetail> visits = new ArrayList<>();
        for (int i = 0; i < records.size(); i++) {
            MedicalRecord record = records.get(i);

            // Get appointment date
            Date appointmentDate = null;
            try {
                AppointmentClientResponse appointment = appointmentClient
                        .getAppointmentForClient(record.getAppointmentId());
                appointmentDate = appointment.getAppointmentDate();
            } catch (Exception e) {
                System.err.println("⚠️ Failed to fetch appointment date for " + record.getAppointmentId());
            }

            // Convert prescriptions
            List<PrescriptionResponse> prescriptions = new ArrayList<>();
            if (record.getPrescriptions() != null && !record.getPrescriptions().isEmpty()) {
                prescriptions = record.getPrescriptions().stream()
                        .map(this::convertToPrescriptionResponse)
                        .collect(Collectors.toList());
            }

            // Calculate visit number (newest = highest number)
            int visitNumber = records.size() - i;

            visits.add(MedicalRecordFullTimelineResponse.VisitDetail.builder()
                    .recordId(record.getRecordId())
                    .appointmentId(record.getAppointmentId())
                    .appointmentDate(appointmentDate)
                    .episodeType(record.getEpisodeType() != null
                            ? record.getEpisodeType().name()
                            : "INITIAL")
                    .parentRecordId(record.getParentRecordId())
                    .isCurrentVisit(record.getRecordId().equals(currentRecordId))
                    .diagnosis(record.getDiagnosis())
                    .symptoms(record.getSymptoms())
                    .treatment(record.getTreatment())
                    .doctorNote(record.getDoctorNote())
                    .serviceName(record.getServiceName())
                    .visitNumberInEpisode(visitNumber)
                    .prescriptions(prescriptions)
                    .createdAt(record.getCreatedAt() != null
                            ? Date.from(record.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                            : null)
                    .build());
        }

        return MedicalRecordFullTimelineResponse.EpisodeGroup.builder()
                .episodeId(rootId)
                .isCurrentEpisode(isCurrentEpisode)
                .firstVisitDate(rootRecord.getCreatedAt() != null
                        ? Date.from(rootRecord.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant())
                        : null)
                .totalVisitsInEpisode(records.size())
                .serviceName(rootRecord.getServiceName())
                .rootDiagnosis(rootRecord.getDiagnosis())
                .visits(visits)
                .build();
    }

    // ========== DASHBOARD METHOD ==========
    @Override
    public List<MedicalRecordDashboardResponse> getRecentMedicalRecordsByDoctor(String doctorId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        Page<MedicalRecord> records = medicalRecordRepository.findRecentByDoctorId(doctorId, pageable);

        return records.getContent().stream()
                .map(record -> MedicalRecordDashboardResponse.builder()
                        .recordId(record.getRecordId())
                        .patientId(record.getPatientId())
                        .diagnosis(record.getDiagnosis())
                        .createdAt(record.getCreatedAt() != null
                                ? Date.valueOf(record.getCreatedAt().toLocalDate())
                                : null)
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public Page<MedicalRecordDetailResponse> getMedicalRecordHistory(String doctorId, String patientId, int page, int size) {
        try {
            // Validate that doctor has treated this patient
            Long recordCount = medicalRecordRepository.countByPatientIdAndDoctorId(patientId, doctorId);
            if (recordCount == 0) {
                throw new NotFoundException("Doctor has not treated this patient");
            }

            // Get paginated medical records
            Pageable pageable = PageRequest.of(page, size);
            Page<MedicalRecord> records = medicalRecordRepository.findByPatientIdAndDoctorId(patientId, doctorId, pageable);

            // Map to MedicalRecordDetailResponse (includes prescriptions)
            return records.map(this::convertToDetailResponse);
        } catch (Exception e) {
            throw new RuntimeException("Error getting medical record history: " + e.getMessage(), e);
        }
    }
}
