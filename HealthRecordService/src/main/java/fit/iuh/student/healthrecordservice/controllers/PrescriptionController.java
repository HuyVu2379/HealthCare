package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
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
}
