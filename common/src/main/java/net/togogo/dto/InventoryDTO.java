package net.togogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryDTO {

    private Long id;
    private String productName;
    private String productCode;
    private String barcode;
    private String supplier;
    private String category;
    private String description;
    private Integer quantity;
    private Double unitPrice;
    private Integer minStock;
    private String location;
    private LocalDateTime createTime;
}