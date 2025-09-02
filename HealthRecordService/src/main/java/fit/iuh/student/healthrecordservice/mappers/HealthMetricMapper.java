package fit.iuh.student.healthrecordservice.mappers;

import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HealthMetricMapper {
    HealthMetricClientResponse toHealthMetricClientResponse(HealthMetric metric);
    HealthMetricResponse toHealthMetricResponse(HealthMetric metric);
}
