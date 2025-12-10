package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.admin.UpdateUserStatusRequest;
import fit.iuh.student.userservice.dtos.admin.UserAdminResponse;
import fit.iuh.student.userservice.dtos.admin.UserStatisticsResponse;
import fit.iuh.student.userservice.dtos.responses.DoctorAdminResponse;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserAdminService {
    
    Page<UserAdminResponse> getUsersWithFilters(Role role, Status status, String search, Pageable pageable);
    
    UserAdminResponse getUserDetails(String userId);
    
    UserAdminResponse updateUserStatus(String userId, UpdateUserStatusRequest request);
    
    UserStatisticsResponse getUserStatistics();
    
    List<DoctorAdminResponse> getDoctorsByIds(List<String> doctorIds);
}
