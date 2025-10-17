package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.CreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateDoctorSchedule;
import fit.iuh.student.schedulingservice.dtos.responses.BulkCreateDoctorScheduleResponse;
import fit.iuh.student.schedulingservice.dtos.responses.DoctorScheduleClientResponse;
import fit.iuh.student.schedulingservice.dtos.responses.DoctorScheduleResponse;

import java.sql.Date;
import java.util.List;

public interface DoctorScheduleService {
    DoctorScheduleResponse createDoctorSchedule(CreateDoctorScheduleRequest request);
    BulkCreateDoctorScheduleResponse bulkCreateDoctorSchedule(BulkCreateScheduleRequest request);
    // Lấy danh sách lịch làm việc theo ngày của bác sĩ
    DoctorScheduleResponse getDoctorScheduleByDate(String doctorId, Date date);
    // Lấy danh sách bác sĩ có lịch làm việc trong ngày
    List<String> getDoctorIdsByDate(Date date);
    boolean updateDoctorSchedule(UpdateDoctorSchedule request);
    List<DoctorScheduleClientResponse> getDoctorByDateAndTimeSlot(Date date, int slotId);
}
