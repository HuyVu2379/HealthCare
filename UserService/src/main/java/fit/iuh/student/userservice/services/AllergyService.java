package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.CreateAllergyRequest;
import fit.iuh.student.userservice.dtos.responses.AllergyResponse;

public interface AllergyService {
    AllergyResponse createAllergy(CreateAllergyRequest request);
}
