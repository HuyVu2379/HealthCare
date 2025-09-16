package fit.iuh.student.userservice.entities;

import fit.iuh.student.userservice.enums.Role;
import fit.iuh.student.userservice.enums.Status;
import fit.iuh.student.userservice.utils.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "doctors")
@PrimaryKeyJoinColumn(name = "user_id")
public class Doctor extends User{
    @Column(name = "specialty")
    private String specialty;
    
    @Column(name = "experience_years")
    private Integer experienceYears;
    
    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "examination_fee")
    private Integer examinationFee;

    @Column(name = "clinic_address")
    private String clinicAddress;

    @Column(name = "rating")
    private Double rating = 0.0;

    @OneToMany(mappedBy = "doctor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Column(name = "medical_histories")
    private List<MedicalHistory> medicalHistories;

    // Lưu certifications dưới dạng JSON trong một cột
    @Column(name = "certifications", columnDefinition = "TEXT")
    @Convert(converter = StringListConverter.class)
    private List<String> certifications;

    public Doctor(String fullName, String email, String phone, String specialty, String password){
        this.setFullName(fullName);
        this.setEmail(email);
        this.setPhone(phone);
        this.setSpecialty(specialty);
        this.setPassword(password);
        this.setRole(Role.DOCTOR);
        this.setStatus(Status.ACTIVE);
    }
}
