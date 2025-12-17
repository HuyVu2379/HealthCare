package fit.iuh.student.adminservice.services;

import fit.iuh.student.adminservice.dtos.revenue.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface AdminRevenueService {

    RevenueOverviewResponse getRevenueOverview(LocalDateTime startDate, LocalDateTime endDate);

    List<RevenueByDateResponse> getRevenueByTime(LocalDateTime startDate, LocalDateTime endDate);

    Page<DoctorRevenueResponse> getRevenueByDoctor(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    List<SpecialtyRevenueResponse> getRevenueBySpecialty(LocalDateTime startDate, LocalDateTime endDate);

    List<ServiceTypeRevenueResponse> getRevenueByServiceType(LocalDateTime startDate, LocalDateTime endDate);

    List<DoctorRevenueResponse> getTopPerformers(LocalDateTime startDate, LocalDateTime endDate, int limit);
}
