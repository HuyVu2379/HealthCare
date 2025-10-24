package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.*;
import fit.iuh.student.healthrecordservice.services.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController()
@RequestMapping("/api/v1/medical-records")
@RequiredArgsConstructor
public class MedicalRecordController {
    private final MedicalRecordService medicalRecordService;

    @PostMapping(value = "/create")
    public ResponseEntity<MessageResponse<CreateMedicalRecordResponse>> createMedicalRecord(
            @RequestBody CreateMedicalRecordRequest request
    ) {
        try {
            CreateMedicalRecordResponse response = medicalRecordService.createMedicalRecord(request);
            return SuccessEntityResponse.ok("Create medical record successfully", response);
        } catch (Exception e) {
            // Log the error for debugging
            System.err.println("Error creating medical record: " + e.getMessage());
            e.printStackTrace();
            
            // Check if record was actually created despite the error
            try {
                // Try to find existing record by appointmentId as fallback
                CreateMedicalRecordResponse existingRecord = medicalRecordService.findByAppointmentId(request.getAppointmentId());
                if (existingRecord != null) {
                    // Record exists, return success response
                    System.out.println("Medical record already exists, returning existing record");
                    return SuccessEntityResponse.ok("Medical record already exists", existingRecord);
                }
            } catch (Exception fallbackError) {
                System.err.println("Error checking existing record: " + fallbackError.getMessage());
            }
            
            // Only return error if no record was found
            return ResponseEntity.status(500).body(
                new MessageResponse<>(500, "Failed to create medical record: " + e.getMessage(), false, null)
            );
        }
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<MessageResponse<MedicalRecordListResponse>> getMedicalRecordsByPatientId(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order
    ) {
        try {
            MedicalRecordListResponse response = medicalRecordService.getMedicalRecordsByPatientId(
                    patientId, page, size, sortBy, order
            );
            return SuccessEntityResponse.ok("Lấy danh sách hồ sơ khám thành công", response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new MessageResponse<>(500, "Không thể tải hồ sơ khám: " + e.getMessage(), false, null)
            );
        }
    }

    @GetMapping("/{recordId}")
    public ResponseEntity<MessageResponse<MedicalRecordDetailResponse>> getMedicalRecordById(
            @PathVariable String recordId
    ) {
        try {
            MedicalRecordDetailResponse response = medicalRecordService.getMedicalRecordById(recordId);
            return SuccessEntityResponse.ok("Lấy thông tin hồ sơ khám thành công", response);
        } catch (Exception e) {
            return ResponseEntity.status(404).body(
                new MessageResponse<>(404, "Không tìm thấy hồ sơ khám: " + e.getMessage(), false, null)
            );
        }
    }

    // ========== NEW ENDPOINTS FOR FOLLOW-UP SYSTEM ==========

    @GetMapping("/{recordId}/timeline")
    public ResponseEntity<MessageResponse<MedicalRecordTimelineResponse>> getMedicalRecordTimeline(
            @PathVariable String recordId
    ) {
        try {
            MedicalRecordTimelineResponse response = medicalRecordService.getMedicalRecordTimeline(recordId);
            return SuccessEntityResponse.ok("Lấy lịch sử khám thành công", response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new MessageResponse<>(500, "Không thể tải lịch sử: " + e.getMessage(), false, null)
            );
        }
    }

    @GetMapping("/patient/{patientId}/episodes")
    public ResponseEntity<MessageResponse<MedicalRecordListResponse>> getPatientEpisodes(
            @PathVariable String patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String order
    ) {
        try {
            MedicalRecordListResponse response = medicalRecordService.getPatientEpisodes(
                    patientId, page, size, sortBy, order
            );
            return SuccessEntityResponse.ok("Lấy danh sách đợt khám thành công", response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    new MessageResponse<>(500, "Không thể tải danh sách: " + e.getMessage(), false, null)
            );
        }
    }
}
