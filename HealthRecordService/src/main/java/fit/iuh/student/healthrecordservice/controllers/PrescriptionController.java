package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionGroupResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.healthrecordservice.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
