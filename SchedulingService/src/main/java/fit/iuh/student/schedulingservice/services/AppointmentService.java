package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;

import java.sql.Date;

public interface AppointmentService {
    void getAppointmentByDoctorIdWithPagination(
            String doctorId, int page, int size, String sortBy, String sortDir
    );
    void getAppointmentByDoctorIdAndDate(
            String doctorId, Date date
    );
    // update appointment status by appointmentId and userId for role doctor and admin
    void updateAppointmentStatus(
            String appointmentId, String userId, AppointmentStatus status
    );
    Appointment bookingAppointment(CreateAppointmentRequest appointment);
    void cancelAppointment(String appointmentId, String userId);
    void getAppointmentByPatientId();
    void getAppointmentDetailById(String appointmentId);
}
