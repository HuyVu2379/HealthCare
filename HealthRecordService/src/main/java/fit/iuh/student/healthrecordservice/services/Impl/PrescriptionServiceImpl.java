package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;
import fit.iuh.student.healthrecordservice.entities.Prescription;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.repositories.PrescriptionRepository;
import fit.iuh.student.healthrecordservice.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    @Override
    public PrescriptionResponse createPrescription(CreatePrescriptionRequest request) {
        try{
            Prescription prescription = Prescription.builder()
                    .medicalRecord(medicalRecordRepository.findById(request.getMedicalRecordId()).orElse(null))
                    .dosage(request.getDosage())
                    .duration(request.getDuration())
                    .frequency(request.getFrequency())
                    .medicalName(request.getMedicalName())
                    .notes(request.getNotes())
                    .build();
            Prescription savedPrescription = prescriptionRepository.save(prescription);
            return PrescriptionResponse.builder()
                    .prescriptionId(savedPrescription.getPrescriptionId())
                    .dosage(savedPrescription.getDosage())
                    .duration(savedPrescription.getDuration())
                    .frequency(savedPrescription.getFrequency())
                    .medicalName(savedPrescription.getMedicalName())
                    .notes(savedPrescription.getNotes())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }
}
