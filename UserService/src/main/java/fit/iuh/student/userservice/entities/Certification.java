package fit.iuh.student.userservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(name = "name", nullable = false)
    private String name;
    
    @Column(name = "issuing_organization", nullable = false)
    private String issuingOrganization;
    
    @Column(name = "year_issued", nullable = false)
    private Integer yearIssued;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private Doctor doctor;
}
