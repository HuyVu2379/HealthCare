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
        return SuccessEntityResponse.ok("Create medical record successfully",
                medicalRecordService.createMedicalRecord(request));
    };
}
