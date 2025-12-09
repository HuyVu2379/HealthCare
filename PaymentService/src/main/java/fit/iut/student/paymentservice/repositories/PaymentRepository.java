package fit.iut.student.paymentservice.repositories;

import fit.iut.student.paymentservice.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, String> {

    Optional<Payment> findByAppointmentId(String appointmentId);

    Optional<Payment> findByOrderCode(Long orderCode);

    List<Payment> findByStatus(String status);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.expiresAt < :now")
    List<Payment> findExpiredPayments(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.appointmentId = :appointmentId ORDER BY p.createdAt DESC")
    List<Payment> findAllByAppointmentId(@Param("appointmentId") String appointmentId);

    // ========== ADMIN QUERIES FOR REVENUE STATISTICS ==========

    /**
     * Get total revenue by date range (only PAID payments)
     */
    @Query("SELECT SUM(p.amount) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate")
    Long sumRevenueBetweenDates(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count paid payments in date range
     */
    @Query("SELECT COUNT(p) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate")
    Long countPaidPayments(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get revenue by date (daily breakdown)
     */
    @Query("SELECT FUNCTION('DATE', p.paidAt) as date, SUM(p.amount) as revenue, COUNT(p) as count " +
           "FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate " +
           "GROUP BY FUNCTION('DATE', p.paidAt) ORDER BY date")
    List<Object[]> getRevenueByDate(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get paid payments by appointment IDs (for joining with appointments)
     */
    @Query("SELECT p FROM Payment p WHERE p.appointmentId IN :appointmentIds AND p.status = 'PAID'")
    List<Payment> findPaidPaymentsByAppointmentIds(@Param("appointmentIds") List<String> appointmentIds);

    /**
     * Get all PAID payments within date range
     */
    @Query("SELECT p FROM Payment p WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate")
    List<Payment> findPaidPaymentsByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Get average payment amount in date range
     */
    @Query("SELECT AVG(p.amount) FROM Payment p " +
           "WHERE p.status = 'PAID' AND p.paidAt BETWEEN :startDate AND :endDate")
    Double avgPaymentAmount(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
