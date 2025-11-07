package fit.iuh.student.schedulingservice.controllers;

import fit.iuh.student.schedulingservice.dtos.requests.CreatePredictRequest;
import fit.iuh.student.schedulingservice.dtos.responses.MessageResponse;
import fit.iuh.student.schedulingservice.dtos.responses.PredictResponse;
import fit.iuh.student.schedulingservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.schedulingservice.services.PredictService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/predicts")
@RequiredArgsConstructor
public class PredictController {
    private final PredictService predictService;

    @PostMapping("/create-predict")
    public ResponseEntity<MessageResponse<Boolean>> createPredict(
            @RequestBody CreatePredictRequest request
            ) {
        return SuccessEntityResponse.created("create predict success!",predictService.createPredictForPatient(request));
    }

    @GetMapping("/get-predict/{patientId}")
    public ResponseEntity<MessageResponse<PredictResponse>> getPredictByPatientId(
            @PathVariable String patientId
    ){
        return SuccessEntityResponse.ok("get predict success!",predictService.getPredictResponseByPatientId(patientId));
    }

//    @GetMapping("/get-predict-history/{patientId}")
//    public ResponseEntity<MessageResponse<List<PredictResponse>>> getPredictHistoryByPatientId(
//            @PathVariable String patientId
//    ){
//        return SuccessEntityResponse.ok("get predict history success!",predictService.getPredictHistoryByPatientId(patientId));
//    }
}
