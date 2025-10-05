package fit.iuh.student.schedulingservice.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Predict extends BaseEntity{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, name = "predict_id")
    private String predictId;
    private String patientId;
    private int state;
    private String recommended;
}
