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

    Optional<Payment> findByOrderCode(String orderCode);

    List<Payment> findByStatus(String status);

    @Query("SELECT p FROM Payment p WHERE p.status = 'PENDING' AND p.expiresAt < :now")
    List<Payment> findExpiredPayments(@Param("now") LocalDateTime now);

    @Query("SELECT p FROM Payment p WHERE p.appointmentId = :appointmentId ORDER BY p.createdAt DESC")
    List<Payment> findAllByAppointmentId(@Param("appointmentId") String appointmentId);
}
