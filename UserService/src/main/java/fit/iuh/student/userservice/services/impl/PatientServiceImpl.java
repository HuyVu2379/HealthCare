package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.dtos.requests.MedicalHistoryRequest;
import fit.iuh.student.userservice.entities.MedicalHistory;
import fit.iuh.student.userservice.entities.Patient;
import fit.iuh.student.userservice.repositories.MedicalHistoryRepository;
import fit.iuh.student.userservice.repositories.PatientRepository;
import fit.iuh.student.userservice.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    @Override
    public Page<MedicalHistory> getMedicalHistoriesByPatientId(String patientId, int page, int size, String sortBy, String sortDir) {
        try{
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt"; // default sort field
            }

            Sort.Direction direction = Sort.Direction.ASC; // default
            if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
                direction = Sort.Direction.DESC;
            }

            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page,size,sort);
            return medicalHistoryRepository.findByPatientUserId(patientId,pageable);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public MedicalHistory updateMedicalHistories(MedicalHistoryRequest request) {
        try{
            Patient patient = patientRepository.findById(request.getUserId()).orElse(null);
            MedicalHistory medicalHistory = new MedicalHistory(patient,request.getCondition(),request.getDiagnosisDate(),request.getNotes());
            return medicalHistoryRepository.save(medicalHistory);
        }catch (Exception e){
            throw e;
        }
    }
}
