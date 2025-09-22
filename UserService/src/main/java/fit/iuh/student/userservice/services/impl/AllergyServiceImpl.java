package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.CreateAllergyRequest;
import fit.iuh.student.userservice.dtos.responses.AllergyResponse;
import fit.iuh.student.userservice.entities.Allergy;
import fit.iuh.student.userservice.repositories.AllergyRepository;
import fit.iuh.student.userservice.repositories.PatientRepository;
import fit.iuh.student.userservice.services.AllergyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AllergyServiceImpl implements AllergyService {
    private final AllergyRepository allergyRepository;
    private final PatientRepository patientRepository;

    @Override
    public AllergyResponse createAllergy(CreateAllergyRequest request) {
        try {
            Allergy a = allergyRepository.save(Allergy.builder()
                    .description(request.getDescription())
                    .level(request.getLevel())
                    .name(request.getName())
                    .patient(patientRepository.findById(request.getPatientId()).orElse(null))
                    .build());
            return AllergyResponse.builder()
                    .allergyId(a.getAllergyId())
                    .description(a.getDescription())
                    .level(a.getLevel())
                    .name(a.getName())
                    .patientId(a.getPatient().getUserId())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }
}
