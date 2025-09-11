package fit.iuh.student.userservice.services.impl;

import fit.iuh.student.userservice.clients.HealthRecordClient;
import fit.iuh.student.userservice.dtos.requests.MedicalHistoryRequest;
import fit.iuh.student.userservice.dtos.requests.UpdatePatientRequest;
import fit.iuh.student.userservice.dtos.responses.DoctorClientResponse;
import fit.iuh.student.userservice.dtos.responses.PatientClientResponse;
import fit.iuh.student.userservice.dtos.responses.PatientResponse;
import fit.iuh.student.userservice.dtos.responses.UpdatePatientResponse;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.entities.MedicalHistory;
import fit.iuh.student.userservice.entities.Patient;
import fit.iuh.student.userservice.mappers.UserMapper;
import fit.iuh.student.userservice.repositories.DoctorRepository;
import fit.iuh.student.userservice.repositories.MedicalHistoryRepository;
import fit.iuh.student.userservice.repositories.PatientRepository;
import fit.iuh.student.userservice.services.PatientService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.time.Period;
import java.util.Comparator;


@Service
@RequiredArgsConstructor
public class PatientServiceImpl implements PatientService {
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserMapper userMapper;
    private final HealthRecordClient healthRecordClient;
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
            Doctor doctor = doctorRepository.findById((request.getDoctorId())).orElse(null);
            MedicalHistory medicalHistory = new MedicalHistory(patient,doctor, request.getServiceName(), request.getDiagnosisDate(),request.getNotes(),request.getStage(),request.getDiagnosis(),request.getStatusHealth());
            return medicalHistoryRepository.save(medicalHistory);
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public UpdatePatientResponse updatePatient(UpdatePatientRequest request) {
        try{
            Patient patient = patientRepository.findById(request.getUserId()).orElse(null);
            if (patient == null) {
                return null; // or throw an exception
            }
            patient.setHeight(request.getHeight());
            patient.setWeight(request.getWeight());
            patient.setBloodType(request.getBloodType());
            patient.setBmi(patient.calculateBMI());
            patientRepository.save(patient);
            return UpdatePatientResponse.builder()
                    .userId(patient.getUserId())
                    .height(patient.getHeight())
                    .weight(patient.getWeight())
                    .bmi(patient.getBmi())
                    .bloodType(patient.getBloodType())
                    .build();
        }catch (Exception e){
            throw e;
        }
    }

    @Override
    public PatientClientResponse getPatientByIdForClient(String patientId) {
        return userMapper.toPatientClientResponse(patientRepository.findById(patientId).get());
    }

    @Override
    public Page<PatientResponse> getPatientsByDoctorId(String doctorId, int page, int size, String sortBy, String sortDir, String namePatient, String statusHealth) {
        try {
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt"; // default sort field
            }

            Sort.Direction direction = Sort.Direction.ASC; // default
            if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
                direction = Sort.Direction.DESC;
            }

            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page-1, size, sort);
            
            Page<Patient> patients = patientRepository.findPatientsByDoctorId(doctorId, pageable, namePatient, statusHealth);
            return patients.map(patient -> {
                // Manual mapping from Patient to PatientResponse
                PatientResponse response = new PatientResponse();
                response.setPatientId(patient.getUserId());
                response.setFullName(patient.getFullName());
                // Calculate age from date of birth if available
                if (patient.getDob() != null) {
                    int age = Period.between(patient.getDob(), LocalDate.now()).getYears();
                    response.setAge(age);
                }
                response.setGender(patient.getGender());
                
                // Get the latest medical history for this patient and doctor
                if (patient.getMedicalHistories() != null && !patient.getMedicalHistories().isEmpty()) {
                    MedicalHistory latestHistory = patient.getMedicalHistories().stream()
                        .filter(mh -> mh.getDoctor() != null && doctorId.equals(mh.getDoctor().getUserId()))
                        .max(Comparator.comparing(MedicalHistory::getDiagnosisDate))
                        .orElse(null);
                    
                    if (latestHistory != null) {
                        // Set last diagnosis date
                        if (latestHistory.getDiagnosisDate() != null) {
                            response.setLastDiagnosisDate(Date.valueOf(latestHistory.getDiagnosisDate()));
                        }
                        response.setHealthMetric(healthRecordClient.getEGFRMetric(patient.getUserId()));
                        // Set status health
                        response.setStatusHealth(latestHistory.getStatusHealth());
                    }
                }
                
                return response;
            });
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
