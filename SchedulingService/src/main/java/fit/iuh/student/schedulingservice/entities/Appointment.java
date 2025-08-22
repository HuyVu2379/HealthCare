package fit.iuh.student.schedulingservice.entities;

import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.enums.ConsultationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Appointment extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "appointment_id")
    private String appointmentId;

    @Column(name = "patient_id", nullable = false)
    private Integer patientId;

    @Column(name = "doctor_id", nullable = false)
    private Integer doctorId;

    @Column(name = "symptoms", length = 500)
    private String symptoms;

    @Column(name = "note", length = 1000)
    private String note;

    @Column(name = "slot_id", nullable = false)
    private Integer slotId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", insertable = false, updatable = false)
    private TimeSlot timeSlot;

    @Column(name = "appointment_date", nullable = false)
    private Date appointmentDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "consultation_type", length = 20, nullable = false)
    private ConsultationType consultationType;

    @Column(name = "address_detail", length = 255)
    private String addressDetail;
}
