package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.RescheduleAppointmentResponse;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import org.springframework.data.domain.Page;

public interface AppointmentService {
//    void getAppointmentByDoctorIdWithPagination(
//            String doctorId, int page, int size, String sortBy, String sortDir
//    );
//    void getAppointmentByDoctorIdAndDate(
//            String doctorId, Date date
//    );
//    // update appointment status by appointmentId and userId for role doctor and admin
//    void updateAppointmentStatus(
//            String appointmentId, String userId, AppointmentStatus status
//    );
    AppointmentResponse bookingAppointment(CreateAppointmentRequest appointment);
    Page<AppointmentResponse> getAppointmentByPatientIdWithPage(String patientId, int page, int size, String sortBy,String startTime,String endTime, String sortDir);
    boolean cancelAppointment(String appointmentId, String userId);
    RescheduleAppointmentResponse rescheduleAppointment(UpdateAppointmentRequest request);
    AppointmentResponse updateAppointmentStatus(String appointmentId,AppointmentStatus status);
    AppointmentResponse getAppointmentDetailById(String appointmentId);
    Page<AppointmentResponse> getAppointmentWithFilterPagination(String type, String status, int page, int size, String sortBy, String sortDir);
//    void cancelAppointment(String appointmentId, String userId);
//    void getAppointmentByPatientId();
//    void getAppointmentDetailById(String appointmentId);
}
