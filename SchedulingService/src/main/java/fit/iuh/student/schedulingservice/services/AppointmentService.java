package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.entities.Appointment;
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
    Page<AppointmentResponse> getAppointmentByPatientIdWithPage(String patientId, int page, int size, String sortBy, String sortDir);
    boolean cancelAppointment(String appointmentId, String userId);
    Appointment rescheduleAppointment(UpdateAppointmentRequest request);
//    void cancelAppointment(String appointmentId, String userId);
//    void getAppointmentByPatientId();
//    void getAppointmentDetailById(String appointmentId);
}
