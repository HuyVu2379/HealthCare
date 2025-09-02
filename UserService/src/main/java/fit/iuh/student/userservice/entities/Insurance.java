package fit.iuh.student.userservice.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
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
    private String insuranceId;
    private String insuranceName;
    private Date insuranceEndDate;
    @OneToOne(mappedBy = "insurance", optional = false)
    private Patient insurancePatient;
}
