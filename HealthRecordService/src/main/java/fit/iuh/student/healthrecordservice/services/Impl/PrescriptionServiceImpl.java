package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;
import fit.iuh.student.healthrecordservice.entities.Prescription;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.repositories.PrescriptionRepository;
import fit.iuh.student.healthrecordservice.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

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
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .frequency(request.getFrequency())
                    .medicalName(request.getMedicalName())
                    .notes(request.getNotes())
                    .build();
            Prescription savedPrescription = prescriptionRepository.save(prescription);

            // Calculate duration properly
            long durationInDays = 0;
            if (savedPrescription.getEndDate() != null && savedPrescription.getStartDate() != null) {
                long diffInMillies = savedPrescription.getEndDate().getTime() - savedPrescription.getStartDate().getTime();
                durationInDays = diffInMillies / (24 * 60 * 60 * 1000);
            }

            return PrescriptionResponse.builder()
                    .prescriptionId(savedPrescription.getPrescriptionId())
                    .dosage(savedPrescription.getDosage())
                    .startDate(savedPrescription.getStartDate())
                    .endDate(savedPrescription.getEndDate())
                    .duration(durationInDays + " ngày")
                    .frequency(savedPrescription.getFrequency())
                    .medicalName(savedPrescription.getMedicalName())
                    .notes(savedPrescription.getNotes())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<PrescriptionResponse> getPrescriptionUsing(String patientId) {
        try{
            List<PrescriptionResponse> prescriptionResponses = prescriptionRepository.findPrescriptionUsing(patientId)
                    .stream()
                    .map(prescription -> {
                        // Calculate duration properly
                        long durationInDays = 0;
                        if (prescription.getEndDate() != null && prescription.getStartDate() != null) {
                            long diffInMillies = prescription.getEndDate().getTime() - prescription.getStartDate().getTime();
                            durationInDays = diffInMillies / (24 * 60 * 60 * 1000);
                        }
                        return PrescriptionResponse.builder()
                                .prescriptionId(prescription.getPrescriptionId())
                                .dosage(prescription.getDosage())
                                .startDate(prescription.getStartDate())
                                .endDate(prescription.getEndDate())
                                .duration(durationInDays + " ngày")
                                .frequency(prescription.getFrequency())
                                .medicalName(prescription.getMedicalName())
                                .notes(prescription.getNotes())
                                .build();
                    })
                    .toList();
            return prescriptionResponses;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<PrescriptionResponse> getPrescriptionsByMedicalRecordId(String medicalRecordId) {
        try {
            List<Prescription> prescriptions = prescriptionRepository.findByMedicalRecordId(medicalRecordId);

            return prescriptions.stream()
                    .map(prescription -> {
                        // Calculate duration properly
                        long durationInDays = 0;
                        if (prescription.getEndDate() != null && prescription.getStartDate() != null) {
                            long diffInMillies = prescription.getEndDate().getTime() - prescription.getStartDate().getTime();
                            durationInDays = diffInMillies / (24 * 60 * 60 * 1000);
                        }
                        return PrescriptionResponse.builder()
                                .prescriptionId(prescription.getPrescriptionId())
                                .dosage(prescription.getDosage())
                                .startDate(prescription.getStartDate())
                                .endDate(prescription.getEndDate())
                                .duration(durationInDays + " ngày")
                                .frequency(prescription.getFrequency())
                                .medicalName(prescription.getMedicalName())
                                .notes(prescription.getNotes())
                                .build();
                    })
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error getting prescriptions by medical record ID: " + e.getMessage(), e);
        }
    }
}
