package fit.iut.student.paymentservice.dtos.admin;

import lombok.*;
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
