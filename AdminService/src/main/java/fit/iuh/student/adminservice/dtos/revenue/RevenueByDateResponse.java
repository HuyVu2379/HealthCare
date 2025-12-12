package fit.iuh.student.adminservice.dtos.revenue;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RevenueByDateResponse {
    private LocalDate date;
    private Long revenue;
    private Long count;
}
