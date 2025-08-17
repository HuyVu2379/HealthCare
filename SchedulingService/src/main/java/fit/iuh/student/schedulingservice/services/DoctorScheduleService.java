package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.responses.BulkCreateDoctorScheduleResponse;

public interface DoctorScheduleService {
    BulkCreateDoctorScheduleResponse bulkCreateDoctorSchedule(BulkCreateDoctorScheduleRequest request);
}
