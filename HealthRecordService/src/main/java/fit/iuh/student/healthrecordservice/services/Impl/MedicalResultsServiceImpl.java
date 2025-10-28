package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.clients.AppointmentClient;
import fit.iuh.student.healthrecordservice.clients.dtos.AppointmentClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.MedicalResultsResponse;
import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import fit.iuh.student.healthrecordservice.entities.Prescription;
import fit.iuh.student.healthrecordservice.exceptions.errors.NotFoundException;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.repositories.PrescriptionRepository;
import fit.iuh.student.healthrecordservice.services.MedicalResultsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MedicalResultsServiceImpl implements MedicalResultsService {
    private final MedicalRecordRepository medicalRecordRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentClient appointmentClient;  // NEW: Inject AppointmentClient

    @Override
    public MedicalResultsResponse getResultsByAppointmentId(String appointmentId, String currentUserId, String currentUserRole) {
        if (appointmentId == null || appointmentId.trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid appointment ID format");
        }

        MedicalRecord mr = medicalRecordRepository.findByAppointmentId(appointmentId);
        if (mr == null) {
            throw new NotFoundException("Medical record not found for this appointment");
        }

        // Access control: allow patient owner or assigned doctor
        boolean isPatientOwner = currentUserId != null && mr.getPatientId().equals(currentUserId) &&
                (currentUserRole != null && currentUserRole.contains("PATIENT"));
        boolean isAssignedDoctor = currentUserId != null && mr.getDoctorId() != null && mr.getDoctorId().equals(currentUserId) &&
                (currentUserRole != null && currentUserRole.contains("DOCTOR"));
        if (!(isPatientOwner || isAssignedDoctor)) {
            throw new SecurityException("Access denied. Only the patient or assigned doctor can view results");
        }

        // Fetch appointment date from AppointmentService
        AppointmentClientResponse appointment = appointmentClient.getAppointmentForClient(appointmentId);

        List<Prescription> prescriptions = prescriptionRepository.findByMedicalRecordId(mr.getRecordId());

        MedicalResultsResponse.MedicalRecordItem mrItem = MedicalResultsResponse.MedicalRecordItem.builder()
                .recordId(mr.getRecordId())
                .appointmentId(mr.getAppointmentId())
                .appointmentDate(appointment.getAppointmentDate())  // NEW: Set appointment date from appointment service
                .diagnosis(mr.getDiagnosis())
                .symptoms(mr.getSymptoms())
                .treatment(mr.getTreatment())
                .doctorNote(mr.getDoctorNote())
                .followUpDate(mr.getFollowUpDate())
                .serviceName(mr.getServiceName())
                .statusHealth(null)
                .createdAt(mr.getCreatedAt())
                .updatedAt(mr.getUpdatedAt())
                .build();

        List<MedicalResultsResponse.PrescriptionItem> presItems = prescriptions.stream().map(p ->
                MedicalResultsResponse.PrescriptionItem.builder()
                        .prescriptionId(p.getPrescriptionId())
                        .medicalRecordId(mr.getRecordId())
                        .medicalName(p.getMedicalName())
                        .dosage(p.getDosage())
                        .frequency(p.getFrequency())
                        .startDate(p.getStartDate())
                        .endDate(p.getEndDate())
                        .notes(p.getNotes())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build()
        ).toList();

        return MedicalResultsResponse.builder()
                .medicalRecord(mrItem)
                .prescriptions(presItems)
                .build();
    }
}


