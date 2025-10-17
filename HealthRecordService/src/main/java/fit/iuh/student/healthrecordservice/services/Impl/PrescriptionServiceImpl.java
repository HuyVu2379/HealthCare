package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.clients.UserClient;
import fit.iuh.student.healthrecordservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.healthrecordservice.dtos.requests.CreatePrescriptionRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionGroupResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.PrescriptionResponse;
import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import fit.iuh.student.healthrecordservice.entities.Prescription;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.repositories.PrescriptionRepository;
import fit.iuh.student.healthrecordservice.services.PrescriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PrescriptionServiceImpl implements PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final MedicalRecordRepository medicalRecordRepository;
    private final UserClient userClient;
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

    @Override
    public List<PrescriptionGroupResponse> getPrescriptionGroups(String patientId) {
        try {
            // Lấy tất cả prescriptions của bệnh nhân, sắp xếp theo ngày tạo
            List<Prescription> allPrescriptions = prescriptionRepository.findAllByPatientIdOrderByCreatedDateDesc(patientId);

            // Nhóm prescriptions theo medicalRecordId
            Map<String, List<Prescription>> groupedByMedicalRecord = allPrescriptions.stream()
                    .collect(Collectors.groupingBy(p -> p.getMedicalRecord().getRecordId()));

            // Tạo PrescriptionGroupResponse cho mỗi nhóm
            List<PrescriptionGroupResponse> groups = new ArrayList<>();

            for (Map.Entry<String, List<Prescription>> entry : groupedByMedicalRecord.entrySet()) {
                String medicalRecordId = entry.getKey();
                List<Prescription> prescriptions = entry.getValue();

                if (prescriptions.isEmpty()) continue;

                // Lấy thông tin từ prescription đầu tiên (vì tất cả đều cùng medical record)
                MedicalRecord medicalRecord = prescriptions.get(0).getMedicalRecord();

                // Lấy thông tin bác sĩ từ UserService
                DoctorClientResponse doctor = null;
                try {
                    doctor = userClient.getDoctorForClient(medicalRecord.getDoctorId());
                } catch (Exception e) {
                    // Nếu không lấy được thông tin bác sĩ, bỏ qua
                }

                // Kiểm tra xem toa thuốc còn hiệu lực không (có ít nhất 1 thuốc chưa hết hạn)
                Date currentDate = new Date();
                boolean isActive = prescriptions.stream()
                        .anyMatch(p -> p.getEndDate() != null && p.getEndDate().after(currentDate));

                // Chuyển đổi các Prescription thành PrescriptionResponse
                List<PrescriptionResponse> prescriptionResponses = prescriptions.stream()
                        .map(prescription -> {
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
                        .collect(Collectors.toList());

                // Chuyển đổi LocalDateTime sang Date cho createdDate
                Date createdDate = null;
                if (medicalRecord.getCreatedAt() != null) {
                    createdDate = Date.from(medicalRecord.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
                }

                // Tạo PrescriptionGroupResponse
                PrescriptionGroupResponse group = PrescriptionGroupResponse.builder()
                        .medicalRecordId(medicalRecordId)
                        .doctorId(medicalRecord.getDoctorId())
                        .doctorName(doctor != null ? doctor.getFullName() : "Unknown Doctor")
                        .createdDate(createdDate)
                        .appointmentDate(null) // Có thể lấy từ appointment nếu cần
                        .diagnosis(medicalRecord.getDiagnosis())
                        .serviceName(medicalRecord.getServiceName())
                        .isActive(isActive)
                        .prescriptions(prescriptionResponses)
                        .totalMedicines(prescriptionResponses.size())
                        .build();

                groups.add(group);
            }

            // Sắp xếp theo ngày tạo mới nhất
            groups.sort((g1, g2) -> {
                if (g1.getCreatedDate() == null) return 1;
                if (g2.getCreatedDate() == null) return -1;
                return g2.getCreatedDate().compareTo(g1.getCreatedDate());
            });

            return groups;
        } catch (Exception e) {
            throw new RuntimeException("Error getting prescription groups: " + e.getMessage(), e);
        }
    }
}
