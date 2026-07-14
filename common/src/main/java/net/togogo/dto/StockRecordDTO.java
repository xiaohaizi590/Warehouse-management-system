package net.togogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.togogo.entity.StockRecord;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRecordDTO {

    private Long id;
    private Long inventoryId;
    private Long userId;
    private String productName;
    private String productCode;
    private String userName;
    private java.time.LocalDateTime recordTime;
    private Integer quantity;
    private Double unitPrice;
    private Double totalAmount;
    private String remark;
    private StockRecord.StockType stockType;
    private StockRecord.StockStatus status;
}