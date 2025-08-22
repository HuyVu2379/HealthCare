package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.requests.InsuranceRequest;
import fit.iuh.student.userservice.dtos.responses.InsuranceResponse;

public interface InsuranceService {
    InsuranceResponse createInsurance(InsuranceRequest insurance);
    boolean deleteInsurance(String insuranceId);
}
