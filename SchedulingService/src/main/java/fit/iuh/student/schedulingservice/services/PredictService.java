package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.CreatePredictRequest;
import fit.iuh.student.schedulingservice.dtos.responses.PredictResponse;

public interface PredictService {
    PredictResponse getPredictResponseByPatientId(String patientId);
    Boolean createPredictForPatient(CreatePredictRequest request);
}
