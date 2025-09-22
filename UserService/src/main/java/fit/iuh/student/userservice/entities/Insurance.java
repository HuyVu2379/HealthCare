package fit.iuh.student.userservice.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Insurance {
    @Id
    @Column(name = "insurance_id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    private String insuranceId;
    private String insuranceName;
    private Date insuranceEndDate;
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient insurancePatient;
}
