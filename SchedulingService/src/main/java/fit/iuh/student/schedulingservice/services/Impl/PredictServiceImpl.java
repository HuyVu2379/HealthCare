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
            // Lấy Authorization header từ request
            String authorizationHeader = getAuthorizationHeader();
            log.debug("Retrieved authorization header for patient ID: {}", patientId);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.warn("No valid authorization header found in request for patient ID: {}", patientId);
                throw new RuntimeException("Authorization header is required");
            }

            List<HealthMetricResponse> healthMetrics = scheduleClient.getHealthMetricsByPatientIdClient(patientId, authorizationHeader);
            Predict predict = predictRepository.findLatestPredictByPatientId(patientId);
            return PredictResponse.builder()
                    .predictId(predict.getPredictId())
                    .patientId(predict.getPatientId())
                    .state(predict.getState())
                    .recommended(predict.getRecommended())
                    .healthMetrics(healthMetrics)
                    .build();
        } catch (Exception e) {
            log.error("Error getting predict response for patient ID: {}", patientId, e);
            throw e;
        }
    }

    @Override
    public PredictResponse createPredictForPatient(CreatePredictRequest request) {
        try{
            // Lấy Authorization header từ request
            String authorizationHeader = getAuthorizationHeader();
            log.debug("Retrieved authorization header for creating predict for patient ID: {}", request.getPatientId());

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                log.warn("No valid authorization header found in request for patient ID: {}", request.getPatientId());
                throw new RuntimeException("Authorization header is required");
            }

            List<HealthMetricResponse> healthMetrics = scheduleClient.createHealthMetrics(request.getHealthMetrics(), authorizationHeader);
            Predict predict = Predict.builder()
                    .patientId(request.getPatientId())
                    .state(request.getState())
                    .recommended(request.getRecommended()).build();
            Predict savedPredict = predictRepository.save(predict);
            return PredictResponse.builder()
                    .predictId(savedPredict.getPredictId())
                    .patientId(savedPredict.getPatientId())
                    .state(savedPredict.getState())
                    .recommended(savedPredict.getRecommended())
                    .healthMetrics(healthMetrics).build();
        }catch (Exception e){
            log.error("Error creating predict for patient ID: {}", request.getPatientId(), e);
            throw e;
        }
    }
}
