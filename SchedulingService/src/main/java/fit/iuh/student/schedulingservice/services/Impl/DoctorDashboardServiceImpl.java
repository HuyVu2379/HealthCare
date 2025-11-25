package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.clients.HealthRecordClient;
import fit.iuh.student.schedulingservice.clients.UserClient;
import fit.iuh.student.schedulingservice.clients.dtos.MedicalRecordClientResponse;
import fit.iuh.student.schedulingservice.clients.dtos.PatientClientResponse;
import fit.iuh.student.schedulingservice.dtos.responses.*;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import fit.iuh.student.schedulingservice.services.DoctorDashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DoctorDashboardServiceImpl implements DoctorDashboardService {
    private final AppointmentRepository appointmentRepository;
    private final UserClient userClient;
    private final HealthRecordClient healthRecordClient;

    @Override
    public DoctorDashboardResponse getDoctorDashboard(String doctorId, Date date) {
        // Get statistics
        DashboardStatisticsDTO statistics = getDashboardStatistics(doctorId, date);

        // Get upcoming appointments
        List<UpcomingAppointmentDTO> upcomingAppointments = getUpcomingAppointments(doctorId, date);

        // Get recent patients
        List<RecentPatientDTO> recentPatients = getRecentPatients(doctorId, 10);

        return DoctorDashboardResponse.builder()
                .statistics(statistics)
                .upcomingAppointments(upcomingAppointments)
                .recentPatients(recentPatients)
                .build();
    }

    private DashboardStatisticsDTO getDashboardStatistics(String doctorId, Date date) {
        // Count today's appointments (PENDING, CONFIRMED, COMPLETED)
        List<AppointmentStatus> countableStatuses = Arrays.asList(
                AppointmentStatus.PENDING,
                AppointmentStatus.CONFIRMED,
                AppointmentStatus.COMPLETED
        );
        Long todayAppointments = appointmentRepository.countTodayAppointmentsByDoctor(
                doctorId, date, countableStatuses);

        // Count new patients today
        Long newPatients = appointmentRepository.countNewPatientsToday(doctorId, date);

        // Count completed consultations today
        Long completedConsultations = appointmentRepository.countCompletedToday(doctorId, date);

        // Count total patients
        Long totalPatients = appointmentRepository.countTotalPatientsByDoctor(doctorId);

        return DashboardStatisticsDTO.builder()
                .todayAppointments(todayAppointments.intValue())
                .newPatients(newPatients.intValue())
                .completedConsultations(completedConsultations.intValue())
                .totalPatients(totalPatients.intValue())
                .build();
    }

    private List<UpcomingAppointmentDTO> getUpcomingAppointments(String doctorId, Date date) {
        List<Appointment> appointments = appointmentRepository.findUpcomingTodayAppointments(doctorId, date);

        return appointments.stream()
                .map(appointment -> {
                    // Get patient info
                    PatientClientResponse patient = userClient.getPatientForClient(appointment.getPatientId());

                    // Format time from timeSlot
                    String time = appointment.getTimeSlot() != null
                            ? appointment.getTimeSlot().getStartTime().toString().substring(0, 5) // HH:mm
                            : "";

                    // Format consultation type
                    String consultationType = formatConsultationType(appointment.getConsultationType());

                    return UpcomingAppointmentDTO.builder()
                            .appointmentId(appointment.getAppointmentId())
                            .time(time)
                            .patientName(patient.getFullName())
                            .consultationType(consultationType)
                            .status(appointment.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<RecentPatientDTO> getRecentPatients(String doctorId, int limit) {
        try {
            // Get recent medical records from HealthRecordService
            List<MedicalRecordClientResponse> medicalRecords =
                    healthRecordClient.getRecentMedicalRecordsByDoctor(doctorId, limit);

            return medicalRecords.stream()
                    .map(record -> {
                        // Get patient info
                        PatientClientResponse patient = userClient.getPatientForClient(record.getPatientId());

                        // Calculate time ago
                        String timeAgo = calculateTimeAgo(record.getCreatedAt());

                        return RecentPatientDTO.builder()
                                .patientId(record.getPatientId())
                                .patientName(patient.getFullName())
                                .diagnosis(record.getDiagnosis())
                                .timeAgo(timeAgo)
                                .build();
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching recent patients: {}", e.getMessage());
            return List.of();
        }
    }

    private String formatConsultationType(fit.iuh.student.schedulingservice.enums.ConsultationType type) {
        if (type == null) return "";

        return switch (type) {
            case DIRECT_CONSULTATION -> "Khám trực tiếp";
            case ONLINE_CONSULTATION -> "Tư vấn trực tuyến";
            case LAB_TEST -> "Xét nghiệm";
            case FOLLOW_UP -> "Tái khám";
            default -> type.toString();
        };
    }

    private String calculateTimeAgo(Date date) {
        if (date == null) return "";

        LocalDateTime recordTime = date.toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        long hours = ChronoUnit.HOURS.between(recordTime, now);
        long days = ChronoUnit.DAYS.between(recordTime, now);
        long weeks = ChronoUnit.WEEKS.between(recordTime, now);
        long months = ChronoUnit.MONTHS.between(recordTime, now);

        if (hours < 24) {
            return hours == 0 ? "Vừa xong" : hours + " giờ trước";
        } else if (days < 7) {
            return days + " ngày trước";
        } else if (weeks < 4) {
            return weeks + " tuần trước";
        } else {
            return months + " tháng trước";
        }
    }
}
