package fit.iuh.student.schedulingservice.services;

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
    void bookingAppointment();
    void cancelAppointment(String appointmentId, String userId);
    void getAppointmentByPatientId();
    void getAppointmentDetailById(String appointmentId);
}
