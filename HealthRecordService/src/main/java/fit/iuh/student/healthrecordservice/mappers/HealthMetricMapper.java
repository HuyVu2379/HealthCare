package fit.iuh.student.healthrecordservice.mappers;

import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricClientResponse;
import fit.iuh.student.healthrecordservice.dtos.responses.HealthMetricResponse;
import fit.iuh.student.healthrecordservice.entities.HealthMetric;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface HealthMetricMapper {
    HealthMetricClientResponse toHealthMetricClientResponse(HealthMetric metric);

    @Mapping(source = "medicalRecord.recordId", target = "medicalRecordId")
    HealthMetricResponse toHealthMetricResponse(HealthMetric metric);
}
