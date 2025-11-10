package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.ScheduleFollowUpByDoctorRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentClientResponse;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentWeekFilterResponse;
import fit.iuh.student.schedulingservice.dtos.responses.RescheduleAppointmentResponse;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import org.springframework.data.domain.Page;

import java.util.List;

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
    Page<AppointmentResponse> getAppointmentWithFilterPagination(String type, AppointmentStatus status, int page, int size, String sortBy, String sortDir);
    Page<AppointmentResponse> getAppointmentWithFilterPaginationForPatient(String patientId,ConsultationType consultationType,int page, int size,String startTime, String endTime);
    List<AppointmentWeekFilterResponse> getAppointmentWeekFilterForDoctor(String doctorId,String weekStartDate, String weekEndDate);
    AppointmentClientResponse getAppointmentDetailForClientById(String appointmentId);
    AppointmentResponse scheduleFollowUpByDoctor(ScheduleFollowUpByDoctorRequest request);

    // Payment-related methods
    void updatePaymentStatus(String appointmentId, String paymentStatus);
    AppointmentResponse confirmAppointmentByDoctor(String appointmentId, String doctorId);
    AppointmentResponse rejectAppointmentByDoctor(String appointmentId, String doctorId, String reason);
//    void cancelAppointment(String appointmentId, String userId);
//    void getAppointmentByPatientId();
//    void getAppointmentDetailById(String appointmentId);
}
