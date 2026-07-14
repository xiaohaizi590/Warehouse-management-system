package net.togogo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_stock_record")
public class StockRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "inventory_id", nullable = false)
    private Long inventoryId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "record_time", nullable = false)
    private java.time.LocalDateTime recordTime;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price")
    private Double unitPrice;

    @Column(name = "total_amount")
    private Double totalAmount;

    @Column(name = "remark", length = 500)
    private String remark;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StockType stockType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private StockStatus status;

    public enum StockType {
        IN,
        OUT
    }

    public enum StockStatus {
        PENDING,
        COMPLETED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (recordTime == null) {
            recordTime = java.time.LocalDateTime.now();
        }
        if (status == null) {
            status = StockStatus.PENDING;
        }
        if (totalAmount == null && unitPrice != null && quantity != null) {
            totalAmount = unitPrice * quantity;
        }
    }
}