package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.InsuranceRequest;
import fit.iuh.student.userservice.dtos.responses.InsuranceResponse;
import fit.iuh.student.userservice.dtos.responses.MessageResponse;
import fit.iuh.student.userservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.services.InsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/insurances")
@RequiredArgsConstructor
public class InsuranceController {
    private final InsuranceService insuranceService;

    @PostMapping("/create")
    public ResponseEntity<MessageResponse<InsuranceResponse>> createInsurance(InsuranceRequest insurance) {
        // Logic to create insurance
        return SuccessEntityResponse.created("Insurance created successfully", insuranceService.createInsurance(insurance));
    }

    @DeleteMapping("/delete/{insuranceId}")
    public ResponseEntity<MessageResponse<Boolean>> deleteInsurance(
            @PathVariable String insuranceId
    ) {
        // Logic to delete insurance
        boolean isDeleted = insuranceService.deleteInsurance(insuranceId);
        if (isDeleted) {
            return SuccessEntityResponse.ok("Insurance deleted successfully", true);
        } else {
            MessageResponse<Boolean> re = new MessageResponse<>(
                    HttpStatus.NOT_FOUND.value(),
                    "Failed to reset password",
                    false,
                    false
            );
            return new ResponseEntity<>(re, HttpStatus.NOT_FOUND);
        }
    }
}
