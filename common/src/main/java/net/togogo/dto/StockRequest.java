package net.togogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {

    private Long inventoryId;
    private Integer quantity;
    private Double unitPrice;
    private String remark;
    private String stockType;
}