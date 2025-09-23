package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.SuccessEntityResponse;
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
    };
}
