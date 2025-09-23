package fit.iuh.student.healthrecordservice.controllers;

import fit.iuh.student.healthrecordservice.dtos.responses.MedicalResultsResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MessageResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.healthrecordservice.services.MedicalResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/medical-results")
@RequiredArgsConstructor
public class MedicalResultController {
    private final MedicalResultsService medicalResultsService;

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('PATIENT','DOCTOR')")
    public ResponseEntity<MessageResponse<MedicalResultsResponse>> getByAppointment(
            @PathVariable String appointmentId,
            Authentication authentication
    ) {
        try {
            String currentUserId = authentication != null ? (String) authentication.getPrincipal() : null;
            String role = authentication != null && authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()
                    ? authentication.getAuthorities().iterator().next().getAuthority()
                    : null;
            MedicalResultsResponse data = medicalResultsService.getResultsByAppointmentId(appointmentId, currentUserId, role);
            return SuccessEntityResponse.ok("Medical results retrieved successfully", data);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new MessageResponse<>(400, e.getMessage(), false, null));
        } catch (SecurityException e) {
            return ResponseEntity.status(403).body(new MessageResponse<>(403, e.getMessage(), false, null));
        } catch (fit.iuh.student.healthrecordservice.exceptions.errors.NotFoundException e) {
            return ResponseEntity.status(404).body(new MessageResponse<>(404, e.getMessage(), false, null));
        }
    }
}


