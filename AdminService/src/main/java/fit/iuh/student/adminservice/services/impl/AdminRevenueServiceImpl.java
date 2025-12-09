package fit.iuh.student.adminservice.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.student.adminservice.clients.PaymentClient;
import fit.iuh.student.adminservice.clients.SchedulingClient;
import fit.iuh.student.adminservice.clients.UserClient;
import fit.iuh.student.adminservice.dtos.revenue.DoctorRevenueResponse;
import fit.iuh.student.adminservice.dtos.revenue.RevenueOverviewResponse;
import fit.iuh.student.adminservice.dtos.revenue.ServiceTypeRevenueResponse;
import fit.iuh.student.adminservice.dtos.revenue.SpecialtyRevenueResponse;
import fit.iuh.student.adminservice.services.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminRevenueServiceImpl implements AdminRevenueService {

    private final PaymentClient paymentClient;
    private final SchedulingClient schedulingClient;
    private final UserClient userClient;
    private final ObjectMapper objectMapper;

    @Override
    public RevenueOverviewResponse getRevenueOverview(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting revenue overview from {} to {}", startDate, endDate);

        try {
            // Get revenue statistics from PaymentService
            ResponseEntity<Object> revenueStatsResponse = paymentClient.getRevenueStatistics(startDate, endDate);
            Map<String, Object> revenueStats = objectMapper.convertValue(revenueStatsResponse.getBody(), Map.class);

            Long totalRevenue = ((Number) revenueStats.getOrDefault("totalRevenue", 0)).longValue();
            Long paymentCount = ((Number) revenueStats.getOrDefault("paymentCount", 0)).longValue();
            Double avgAmount = ((Number) revenueStats.getOrDefault("averagePaymentAmount", 0.0)).doubleValue();

            // Get appointment statistics from SchedulingService
            java.time.LocalDate startLocalDate = startDate.toLocalDate();
            java.time.LocalDate endLocalDate = endDate.toLocalDate();
            ResponseEntity<Object> appointmentStatsResponse = schedulingClient.getStatistics(startLocalDate, endLocalDate);
            Map<String, Object> appointmentStats = objectMapper.convertValue(appointmentStatsResponse.getBody(), Map.class);

            Long totalAppointments = ((Number) appointmentStats.getOrDefault("totalAppointments", 0)).longValue();

            return RevenueOverviewResponse.builder()
                    .totalRevenue(totalRevenue)
                    .totalAppointments(totalAppointments)
                    .averagePaymentAmount(avgAmount)
                    .completedAppointments(paymentCount)
                    .build();

        } catch (Exception e) {
            log.error("Error getting revenue overview", e);
            throw new RuntimeException("Failed to get revenue overview: " + e.getMessage());
        }
    }

    @Override
    public List<Object> getRevenueByTime(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting revenue by time from {} to {}", startDate, endDate);

        try {
            ResponseEntity<List<Object>> response = paymentClient.getRevenueByDate(startDate, endDate);
            return response.getBody();
        } catch (Exception e) {
            log.error("Error getting revenue by time", e);
            throw new RuntimeException("Failed to get revenue by time: " + e.getMessage());
        }
    }

    @Override
    public Page<DoctorRevenueResponse> getRevenueByDoctor(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        log.info("Getting revenue by doctor from {} to {}", startDate, endDate);

        try {
            // Step 1: Get all PAID payments in date range
            ResponseEntity<List<Object>> paymentsResponse = paymentClient.getPaidPaymentsByDateRange(startDate, endDate);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> payments = paymentsResponse.getBody().stream()
                    .map(p -> (Map<String, Object>) objectMapper.convertValue(p, Map.class))
                    .collect(Collectors.toList());

            if (payments.isEmpty()) {
                return new PageImpl<>(Collections.emptyList(), pageable, 0);
            }

            // Step 2: Extract unique appointmentIds
            List<String> appointmentIds = payments.stream()
                    .map(p -> (String) p.get("appointmentId"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // Step 3: Get appointments to link to doctors
            ResponseEntity<List<Object>> appointmentsResponse = schedulingClient.getAppointmentsByIds(appointmentIds);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> appointments = appointmentsResponse.getBody().stream()
                    .map(a -> (Map<String, Object>) objectMapper.convertValue(a, Map.class))
                    .collect(Collectors.toList());

            // Step 4: Build map appointmentId -> doctorId
            Map<String, String> apptToDoctorMap = appointments.stream()
                    .collect(Collectors.toMap(
                            a -> (String) a.get("appointmentId"),
                            a -> (String) a.get("doctorId"),
                            (existing, replacement) -> existing
                    ));

            // Step 5: Aggregate revenue and count by doctor
            Map<String, Long> revenueByDoctor = new HashMap<>();
            Map<String, Long> countByDoctor = new HashMap<>();

            for (Map<String, Object> payment : payments) {
                String appointmentId = (String) payment.get("appointmentId");
                String doctorId = apptToDoctorMap.get(appointmentId);

                if (doctorId != null) {
                    Long amount = ((Number) payment.get("amount")).longValue();
                    revenueByDoctor.merge(doctorId, amount, Long::sum);
                    countByDoctor.merge(doctorId, 1L, Long::sum);
                }
            }

            // Step 6: Get doctor details
            List<String> doctorIds = new ArrayList<>(revenueByDoctor.keySet());
            ResponseEntity<List<Object>> doctorsResponse = userClient.getDoctorsByIds(doctorIds);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> doctors = doctorsResponse.getBody().stream()
                    .map(d -> (Map<String, Object>) objectMapper.convertValue(d, Map.class))
                    .collect(Collectors.toList());

            // Step 7: Build response
            List<DoctorRevenueResponse> responses = doctors.stream()
                    .map(doctor -> {
                        String doctorId = (String) doctor.get("userId");
                        return DoctorRevenueResponse.builder()
                                .doctorId(doctorId)
                                .doctorName((String) doctor.get("fullName"))
                                .specialty((String) doctor.get("specialty"))
                                .totalRevenue(revenueByDoctor.get(doctorId))
                                .appointmentCount(countByDoctor.get(doctorId))
                                .rating(doctor.get("rating") != null ? ((Number) doctor.get("rating")).doubleValue() : 0.0)
                                .build();
                    })
                    .sorted(Comparator.comparing(DoctorRevenueResponse::getTotalRevenue).reversed())
                    .collect(Collectors.toList());

            // Step 8: Apply pagination
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), responses.size());

            List<DoctorRevenueResponse> paginatedList = start >= responses.size() ?
                    Collections.emptyList() : responses.subList(start, end);

            return new PageImpl<>(paginatedList, pageable, responses.size());

        } catch (Exception e) {
            log.error("Error getting revenue by doctor", e);
            throw new RuntimeException("Failed to get revenue by doctor: " + e.getMessage());
        }
    }

    @Override
    public List<SpecialtyRevenueResponse> getRevenueBySpecialty(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting revenue by specialty from {} to {}", startDate, endDate);

        try {
            // Get all doctor revenues first (without pagination)
            Pageable unpaged = Pageable.unpaged();
            Page<DoctorRevenueResponse> doctorRevenues = getRevenueByDoctor(startDate, endDate, unpaged);

            // Group by specialty
            Map<String, Long> revenueBySpecialty = new HashMap<>();
            Map<String, Long> countBySpecialty = new HashMap<>();

            for (DoctorRevenueResponse doctor : doctorRevenues.getContent()) {
                String specialty = doctor.getSpecialty() != null ? doctor.getSpecialty() : "Unknown";
                revenueBySpecialty.merge(specialty, doctor.getTotalRevenue(), Long::sum);
                countBySpecialty.merge(specialty, doctor.getAppointmentCount(), Long::sum);
            }

            // Calculate total revenue for percentage
            Long totalRevenue = revenueBySpecialty.values().stream().mapToLong(Long::longValue).sum();

            // Build response
            return revenueBySpecialty.entrySet().stream()
                    .map(entry -> SpecialtyRevenueResponse.builder()
                            .specialty(entry.getKey())
                            .totalRevenue(entry.getValue())
                            .appointmentCount(countBySpecialty.get(entry.getKey()))
                            .percentage(totalRevenue > 0 ? (entry.getValue() * 100.0 / totalRevenue) : 0.0)
                            .build())
                    .sorted(Comparator.comparing(SpecialtyRevenueResponse::getTotalRevenue).reversed())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting revenue by specialty", e);
            throw new RuntimeException("Failed to get revenue by specialty: " + e.getMessage());
        }
    }

    @Override
    public List<ServiceTypeRevenueResponse> getRevenueByServiceType(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Getting revenue by service type from {} to {}", startDate, endDate);

        try {
            // Step 1: Get all PAID payments
            ResponseEntity<List<Object>> paymentsResponse = paymentClient.getPaidPaymentsByDateRange(startDate, endDate);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> payments = paymentsResponse.getBody().stream()
                    .map(p -> (Map<String, Object>) objectMapper.convertValue(p, Map.class))
                    .collect(Collectors.toList());

            if (payments.isEmpty()) {
                return Collections.emptyList();
            }

            // Step 2: Extract appointmentIds
            List<String> appointmentIds = payments.stream()
                    .map(p -> (String) p.get("appointmentId"))
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());

            // Step 3: Get appointments with consultation types
            ResponseEntity<List<Object>> appointmentsResponse = schedulingClient.getAppointmentsByIds(appointmentIds);
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> appointments = appointmentsResponse.getBody().stream()
                    .map(a -> (Map<String, Object>) objectMapper.convertValue(a, Map.class))
                    .collect(Collectors.toList());

            // Step 4: Build map appointmentId -> consultationType
            Map<String, String> apptToTypeMap = appointments.stream()
                    .collect(Collectors.toMap(
                            a -> (String) a.get("appointmentId"),
                            a -> (String) a.get("consultationType"),
                            (existing, replacement) -> existing
                    ));

            // Step 5: Aggregate revenue by service type
            Map<String, Long> revenueByType = new HashMap<>();
            Map<String, Long> countByType = new HashMap<>();

            for (Map<String, Object> payment : payments) {
                String appointmentId = (String) payment.get("appointmentId");
                String serviceType = apptToTypeMap.get(appointmentId);

                if (serviceType != null) {
                    Long amount = ((Number) payment.get("amount")).longValue();
                    revenueByType.merge(serviceType, amount, Long::sum);
                    countByType.merge(serviceType, 1L, Long::sum);
                }
            }

            // Calculate total for percentage
            Long totalRevenue = revenueByType.values().stream().mapToLong(Long::longValue).sum();

            // Build response
            return revenueByType.entrySet().stream()
                    .map(entry -> ServiceTypeRevenueResponse.builder()
                            .serviceType(entry.getKey())
                            .totalRevenue(entry.getValue())
                            .appointmentCount(countByType.get(entry.getKey()))
                            .percentage(totalRevenue > 0 ? (entry.getValue() * 100.0 / totalRevenue) : 0.0)
                            .build())
                    .sorted(Comparator.comparing(ServiceTypeRevenueResponse::getTotalRevenue).reversed())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting revenue by service type", e);
            throw new RuntimeException("Failed to get revenue by service type: " + e.getMessage());
        }
    }

    @Override
    public List<DoctorRevenueResponse> getTopPerformers(LocalDateTime startDate, LocalDateTime endDate, int limit) {
        log.info("Getting top {} performers from {} to {}", limit, startDate, endDate);

        try {
            Pageable unpaged = Pageable.unpaged();
            Page<DoctorRevenueResponse> allDoctors = getRevenueByDoctor(startDate, endDate, unpaged);

            return allDoctors.getContent().stream()
                    .limit(limit)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error getting top performers", e);
            throw new RuntimeException("Failed to get top performers: " + e.getMessage());
        }
    }
}
