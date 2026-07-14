package net.togogo.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInventoryRequest {

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
}