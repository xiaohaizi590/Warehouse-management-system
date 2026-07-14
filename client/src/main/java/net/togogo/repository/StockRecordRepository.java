package net.togogo.repository;

import net.togogo.entity.StockRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StockRecordRepository extends JpaRepository<StockRecord, Long> {
    List<StockRecord> findByUserIdOrderByRecordTimeDesc(Long userId);
    Page<StockRecord> findByUserId(Long userId, Pageable pageable);
    List<StockRecord> findByInventoryIdOrderByRecordTimeDesc(Long inventoryId);
    Page<StockRecord> findByStockType(StockRecord.StockType stockType, Pageable pageable);
    Page<StockRecord> findByStatus(StockRecord.StockStatus status, Pageable pageable);
    Page<StockRecord> findByStockTypeAndStatus(StockRecord.StockType stockType, StockRecord.StockStatus status, Pageable pageable);
    Long countByInventoryIdAndStatus(Long inventoryId, StockRecord.StockStatus status);
}