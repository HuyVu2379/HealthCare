package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.MedicalHistoryRequest;
import fit.iuh.student.userservice.dtos.responses.MessageResponse;
import fit.iuh.student.userservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.userservice.entities.MedicalHistory;
import fit.iuh.student.userservice.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    @GetMapping("/getMedicalHistoryWithPageForPatientId")
    public ResponseEntity<MessageResponse<Page<MedicalHistory>>> getMedicalHistoryPatientId(
            @RequestParam String patientId,
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String sortDir,
            @RequestParam String sortBy
    ){
        return SuccessEntityResponse.ok("get MedicalHistory by patient success!",patientService.getMedicalHistoriesByPatientId(patientId, page, size, sortBy, sortDir));
    }

    @PostMapping("/updateMedicalHistoryForPatient")
    public ResponseEntity<MessageResponse<MedicalHistory>> updateMedicalHistoryForPatient(
            @RequestBody MedicalHistoryRequest medicalHistory
    ){
        return SuccessEntityResponse.ok("update medical history for patient success!",patientService.updateMedicalHistories(medicalHistory));
    }
}
