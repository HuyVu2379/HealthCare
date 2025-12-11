package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.CreatePredictRequest;
import fit.iuh.student.schedulingservice.dtos.responses.PredictResponse;

import java.util.List;

public interface PredictService {
    PredictResponse getPredictResponseByPatientId(String patientId);
    PredictResponse createPredictForPatient(CreatePredictRequest request);
//    List<PredictResponse> getPredictHistoryByPatientId(String patientId);
}
