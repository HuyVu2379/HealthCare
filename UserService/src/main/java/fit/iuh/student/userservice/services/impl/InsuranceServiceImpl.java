package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.InsuranceRequest;
import fit.iuh.student.userservice.dtos.responses.InsuranceResponse;
import fit.iuh.student.userservice.entities.Insurance;
import fit.iuh.student.userservice.repositories.InsuranceRepository;
import fit.iuh.student.userservice.repositories.PatientRepository;
import fit.iuh.student.userservice.services.InsuranceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InsuranceServiceImpl implements InsuranceService {
    private final InsuranceRepository insuranceRepository;
    private final PatientRepository patientRepository;

    @Override
    public InsuranceResponse createInsurance(InsuranceRequest insurance) {
        try {
            Insurance newInsurance = new Insurance(insurance.getInsuranceId(),
                    insurance.getInsuranceName(),
                    insurance.getInsuranceEndDate(),
                    patientRepository.findById(insurance.getPatientId()).orElse(null));
            insuranceRepository.save(newInsurance);
            return InsuranceResponse.builder()
                    .insuranceId(newInsurance.getInsuranceId())
                    .insuranceName(newInsurance.getInsuranceName())
                    .insuranceEndDate(newInsurance.getInsuranceEndDate())
                    .patientId(newInsurance.getInsurancePatient().getUserId())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean deleteInsurance(String insuranceId) {
        try{
            Insurance insurance = insuranceRepository.findById(insuranceId).orElse(null);
            if (insurance != null) {
                insuranceRepository.delete(insurance);
                return true;
            }
            return false;
        }catch (Exception e){
            throw e;
        }
    }
}
