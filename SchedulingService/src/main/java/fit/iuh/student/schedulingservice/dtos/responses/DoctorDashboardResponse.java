package fit.iuh.student.schedulingservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDashboardResponse {
    private DashboardStatisticsDTO statistics;
    private List<UpcomingAppointmentDTO> upcomingAppointments;
    private List<RecentPatientDTO> recentPatients;
}
