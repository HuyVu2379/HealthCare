package fit.iuh.student.userservice.services.Impl;

import fit.iuh.student.userservice.dtos.admin.UpdateUserStatusRequest;
import fit.iuh.student.userservice.dtos.admin.UserAdminResponse;
import fit.iuh.student.userservice.dtos.admin.UserStatisticsResponse;
import fit.iuh.student.userservice.entities.Doctor;
import fit.iuh.student.userservice.entities.User;
import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import fit.iuh.student.userservice.repositories.DoctorRepository;
import fit.iuh.student.userservice.repositories.UserRepository;
import fit.iuh.student.userservice.services.UserAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAdminServiceImpl implements UserAdminService {

    private final UserRepository userRepository;
    private final DoctorRepository doctorRepository;

    @Override
    public Page<UserAdminResponse> getUsersWithFilters(Role role, Status status, String search, Pageable pageable) {
        log.info("Getting users with filters - role: {}, status: {}, search: {}", role, status, search);
        
        Page<User> users = userRepository.findUsersWithFilters(role, status, search, pageable);
        
        return users.map(this::convertToAdminResponse);
    }

    @Override
    public UserAdminResponse getUserDetails(String userId) {
        log.info("Getting user details for userId: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        return convertToAdminResponse(user);
    }

    @Override
    @Transactional
    public UserAdminResponse updateUserStatus(String userId, UpdateUserStatusRequest request) {
        log.info("Updating user status for userId: {} to status: {}, reason: {}", 
                userId, request.getNewStatus(), request.getReason());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        
        user.setStatus(request.getNewStatus());
        User updatedUser = userRepository.save(user);
        
        log.info("Successfully updated user status for userId: {}", userId);
        return convertToAdminResponse(updatedUser);
    }

    @Override
    public UserStatisticsResponse getUserStatistics() {
        log.info("Getting user statistics");
        
        // Get statistics by role
        List<Object[]> roleStats = userRepository.getUserStatisticsByRole();
        Map<Role, Long> usersByRole = new HashMap<>();
        for (Object[] stat : roleStats) {
            usersByRole.put((Role) stat[0], (Long) stat[1]);
        }
        
        // Get statistics by status
        List<Object[]> statusStats = userRepository.getUserStatisticsByStatus();
        Map<Status, Long> usersByStatus = new HashMap<>();
        for (Object[] stat : statusStats) {
            usersByStatus.put((Status) stat[0], (Long) stat[1]);
        }
        
        // Calculate totals
        Long totalUsers = userRepository.count();
        Long activeUsers = userRepository.countByStatus(Status.ACTIVE);
        Long inactiveUsers = userRepository.countByStatus(Status.INACTIVE);
        Long blockedUsers = userRepository.countByStatus(Status.BLOCKED);
        
        return UserStatisticsResponse.builder()
                .usersByRole(usersByRole)
                .usersByStatus(usersByStatus)
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .blockedUsers(blockedUsers)
                .build();
    }

    @Override
    public List<Doctor> getDoctorsByIds(List<String> doctorIds) {
        log.info("Getting doctors by IDs, count: {}", doctorIds.size());
        return doctorRepository.findByUserIdIn(doctorIds);
    }

    private UserAdminResponse convertToAdminResponse(User user) {
        return UserAdminResponse.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .role(user.getRole())
                .status(user.getStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
