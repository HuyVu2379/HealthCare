package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.CreateAllergyRequest;
import fit.iuh.student.userservice.dtos.responses.AllergyResponse;
import fit.iuh.student.userservice.dtos.responses.MessageResponse;
import fit.iuh.student.userservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.userservice.exceptions.errors.NotFoundException;
import fit.iuh.student.userservice.repositories.AllergyRepository;
import fit.iuh.student.userservice.services.AllergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/allergies")
@RequiredArgsConstructor
public class AllergyController {
    private final AllergyService allergyService;
    private final AllergyRepository allergyRepository;

    @PostMapping("/create")
    public ResponseEntity<MessageResponse<AllergyResponse>> createAllergy(@RequestBody CreateAllergyRequest request) {
        return SuccessEntityResponse.created("create allergy success!", allergyService.createAllergy(request));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<MessageResponse<Boolean>> deleteAllergy(@PathVariable("id") String id) {
        boolean existAllergy = allergyRepository.existsById(id);
        if (existAllergy) {
            allergyRepository.delete(allergyRepository.findById(id).orElse(null));
            return SuccessEntityResponse.ok("delete allergy success!", true);
        }
        throw new NotFoundException("Not found Allergy with id: " + id);
    }
}
