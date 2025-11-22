package fit.iut.student.paymentservice.dtos.payos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemData {
    private String name;
    private Integer quantity;
    private Integer price;
}
