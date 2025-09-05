package fit.iuh.student.healthrecordservice.services.Impl;

import fit.iuh.student.healthrecordservice.dtos.requests.CreateMedicalRecordRequest;
import fit.iuh.student.healthrecordservice.dtos.responses.CreateMedicalRecordResponse;
import fit.iuh.student.healthrecordservice.entities.MedicalRecord;
import fit.iuh.student.healthrecordservice.exceptions.errors.DuplicationObjectException;
import fit.iuh.student.healthrecordservice.repositories.MedicalRecordRepository;
import fit.iuh.student.healthrecordservice.services.MedicalRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.sql.Date;

@Service
@RequiredArgsConstructor
public class MedicalRecordServiceImpl implements MedicalRecordService {
    private final MedicalRecordRepository medicalRecordRepository;
    @Override
    public CreateMedicalRecordResponse createMedicalRecord(CreateMedicalRecordRequest request) {
        try{
            boolean isExist = medicalRecordRepository.existsAppointmentId(request.getAppointmentId());
            if(isExist){
                throw new DuplicationObjectException("Appointment ID already exists");
            }
            MedicalRecord medicalRecord = MedicalRecord.builder()
                    .appointmentId(request.getAppointmentId())
                    .diagnosis(request.getDiagnosis())
                    .doctorNote(request.getDoctorNote())
                    .followUpDate(Date.valueOf(request.getFollowUpDate()))
                    .healthMetrics(null)
                    .prescriptions(null)
                    .imageAttachments(request.getImageAttachments())
                    .symptoms(request.getSymptoms())
                    .treatment(request.getTreatment())
                    .build();
            medicalRecord = medicalRecordRepository.save(medicalRecord);
            return CreateMedicalRecordResponse.builder()
                    .recordId(medicalRecord.getRecordId())
                    .appointmentId(medicalRecord.getAppointmentId())
                    .diagnosis(medicalRecord.getDiagnosis())
                    .doctorNote(medicalRecord.getDoctorNote())
                    .followUpDate(medicalRecord.getFollowUpDate())
                    .imageAttachments(medicalRecord.getImageAttachments())
                    .symptoms(medicalRecord.getSymptoms())
                    .treatment(medicalRecord.getTreatment())
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }
}
