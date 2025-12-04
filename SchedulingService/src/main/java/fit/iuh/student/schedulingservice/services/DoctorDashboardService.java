package fit.iuh.student.schedulingservice.services;

import fit.iuh.student.schedulingservice.dtos.responses.DoctorDashboardResponse;

import java.sql.Date;

public interface DoctorDashboardService {
    DoctorDashboardResponse getDoctorDashboard(String doctorId, Date date);
}
