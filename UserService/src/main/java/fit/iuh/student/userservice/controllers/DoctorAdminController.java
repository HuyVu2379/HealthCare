package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.services.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors/admin")
@RequiredArgsConstructor
@Slf4j
public class DoctorAdminController {

    private final UserAdminService userAdminService;

    /**
     * Get multiple doctors by IDs (for admin revenue aggregation)
     * Admin only endpoint
     */
    @PostMapping("/by-ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Doctor>> getDoctorsByIds(@RequestBody List<String> doctorIds) {
        log.info("Admin request: Get doctors by IDs, count: {}", doctorIds.size());
        List<Doctor> doctors = userAdminService.getDoctorsByIds(doctorIds);
        return ResponseEntity.ok(doctors);
    }
}
