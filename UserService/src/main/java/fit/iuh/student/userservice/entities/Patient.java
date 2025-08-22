package fit.iuh.student.userservice.entities;

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
@Table(name = "patients")
@PrimaryKeyJoinColumn(name = "user_id")
public class Patient extends User {
    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MedicalHistory> medicalHistories;
    private int height; // in cm
    private double weight; // in kg
    private String bloodType; // e.g., A+, O-, etc.
    private double bmi; // Body Mass Index
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Insurance insurance;

    public double calculateBMI() {
        if (weight <= 0 || height <= 0) {
            return 0; // Avoid division by zero
        }
        double heightInMeters = height / 100.0;
        return weight / (heightInMeters * heightInMeters);
    }
}
