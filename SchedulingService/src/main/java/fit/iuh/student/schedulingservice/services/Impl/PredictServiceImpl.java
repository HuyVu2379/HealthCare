package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.clients.ScheduleClient;
import fit.iuh.student.schedulingservice.dtos.requests.CreatePredictRequest;
import fit.iuh.student.schedulingservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.schedulingservice.dtos.responses.PredictResponse;
import fit.iuh.student.schedulingservice.entities.Predict;
import fit.iuh.student.schedulingservice.repositories.PredictRepository;
import fit.iuh.student.schedulingservice.services.PredictService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PredictServiceImpl implements PredictService {
    private final PredictRepository predictRepository;
    private final ScheduleClient scheduleClient;

    private String getAuthorizationHeader() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return request.getHeader("Authorization");
        }
        return null;
    }

    @Override
    public PredictResponse getPredictResponseByPatientId(String patientId) {
        try{
//            // Lấy Authorization header từ request
//            String authorizationHeader = getAuthorizationHeader();
//            log.debug("Retrieved authorization header for patient ID: {}", patientId);
//
//            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
//                log.warn("No valid authorization header found in request for patient ID: {}", patientId);
//                throw new RuntimeException("Authorization header is required");
//            }

//            List<HealthMetricResponse> healthMetrics = scheduleClient.getHealthMetricsByPatientIdClient(patientId, authorizationHeader);
            List<HealthMetricResponse> healthMetrics = scheduleClient.getHealthMetricsByPatientIdClient(patientId);
            Predict predict = predictRepository.findLatestPredictByPatientId(patientId);
            
            // Check null before accessing
            if (predict == null) {
                log.info("No predict data found for patient ID: {}", patientId);
                return null; // Return null when no predict data exists
            }
            
            return PredictResponse.builder()
                    .predictId(predict.getPredictId())
                    .patientId(predict.getPatientId())
                    .stage(predict.getStage())
                    .recommendations(predict.getRecommendations())
                    .healthMetrics(healthMetrics)
                    .confidence(predict.getConfidence())
                    .build();
        } catch (Exception e) {
            log.error("Error getting predict response for patient ID: {}", patientId, e);
            throw e;
        }
    }

    @Override
    public Boolean createPredictForPatient(CreatePredictRequest request) {
        try{
            // Lấy Authorization header từ request
            String authorizationHeader = getAuthorizationHeader();
            log.debug("Retrieved authorization header for creating predict for patient ID: {}", request.getPatientId());

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.warn("No valid authorization header found in request for patient ID: {}", request.getPatientId());
                throw new RuntimeException("Authorization header is required");
            }

            List<HealthMetricResponse> healthMetrics = scheduleClient.createHealthMetrics(request.getHealthMetrics(), authorizationHeader);
            if(healthMetrics == null || healthMetrics.isEmpty()){
                log.error("Failed to create health metrics for patient ID: {}", request.getPatientId());
                throw new RuntimeException("Failed to create health metrics");
            }
            Predict predict = Predict.builder()
                    .patientId(request.getPatientId())
                    .stage(request.getStage())
                    .recommendations(request.getRecommendations())
                    .confidence(request.getConfidence())
                    .build();
            predictRepository.save(predict);
            log.info("Successfully created predict for patient ID: {}", request.getPatientId());
            return true;
        }catch (Exception e){
            log.error("Error creating predict for patient ID: {}", request.getPatientId(), e);
            throw e;
        }
    }
}
