package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionGroupResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.healthrecordservice.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
@RequiredArgsConstructor
public class PrescriptionController {
    private final PrescriptionService prescriptionService;
    @PostMapping("create")
    public ResponseEntity<MessageResponse<PrescriptionResponse>> createPrescription(
            @RequestBody CreatePrescriptionRequest request
    ) {
        return SuccessEntityResponse.ok("Create prescription successfully", prescriptionService.createPrescription(request));
    }

    @GetMapping("get-using/{patientId}")
    public ResponseEntity<MessageResponse<List<PrescriptionResponse>>> getPrescriptionUsing(
            @PathVariable String patientId
    ){
        return SuccessEntityResponse.ok("Get prescription using successfully", prescriptionService.getPrescriptionUsing(patientId));
    }

    @GetMapping("/medical-record/{medicalRecordId}")
    public ResponseEntity<MessageResponse<List<PrescriptionResponse>>> getPrescriptionsByMedicalRecordId(
            @PathVariable String medicalRecordId
    ) {
        try {
            List<PrescriptionResponse> prescriptions = prescriptionService.getPrescriptionsByMedicalRecordId(medicalRecordId);
            return SuccessEntityResponse.ok("Lấy danh sách đơn thuốc thành công", prescriptions);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new MessageResponse<>(500, "Không thể tải đơn thuốc: " + e.getMessage(), false, null)
            );
        }
    }

    @GetMapping("/groups/{patientId}")
    public ResponseEntity<MessageResponse<List<PrescriptionGroupResponse>>> getPrescriptionGroups(
            @PathVariable String patientId
    ) {
        try {
            List<PrescriptionGroupResponse> groups = prescriptionService.getPrescriptionGroups(patientId);
            return SuccessEntityResponse.ok("Lấy danh sách toa thuốc thành công", groups);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                new MessageResponse<>(500, "Không thể tải danh sách toa thuốc: " + e.getMessage(), false, null)
            );
        }
    }

    /**
     * Download prescription as PDF
     *
     * @param recordId Medical record ID
     * @return PDF file
     */
    @GetMapping("/download/{recordId}")
    public ResponseEntity<byte[]> downloadPrescriptionPdf(@PathVariable String recordId) {
        try {
            // Generate PDF (returns byte array and appointment date)
            byte[] pdfBytes = prescriptionService.downloadPrescriptionPdf(recordId);

            // Generate filename using appointment date from medical record
            String filename = prescriptionService.generatePrescriptionFilename(recordId);

            // Set headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDisposition(
                    ContentDisposition.builder("attachment")
                            .filename(filename, StandardCharsets.UTF_8)
                            .build()
            );
            headers.setContentLength(pdfBytes.length);

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (IllegalArgumentException e) {
            // Bad request: record not found or no prescriptions
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (AccessDeniedException e) {
            // Forbidden: user not authorized
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            // Internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
