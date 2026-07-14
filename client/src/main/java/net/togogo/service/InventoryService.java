package net.togogo.service;

import net.togogo.dto.InventoryDTO;
import net.togogo.dto.StockRecordDTO;
import net.togogo.dto.CreateInventoryRequest;
import net.togogo.dto.StockRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface InventoryService {
    InventoryDTO createInventory(CreateInventoryRequest request);
    InventoryDTO getInventoryById(Long id);
    Page<InventoryDTO> getAllInventories(Pageable pageable);
    Page<InventoryDTO> searchByProductName(String productName, Pageable pageable);
    Page<InventoryDTO> searchByCategory(String category, Pageable pageable);
    Page<InventoryDTO> searchBySupplier(String supplier, Pageable pageable);
    Page<InventoryDTO> getLowStockItems(Integer threshold, Pageable pageable);
    InventoryDTO updateInventory(Long id, CreateInventoryRequest request);
    void deleteInventory(Long id);

    StockRecordDTO stockIn(Long userId, StockRequest request);
    StockRecordDTO stockOut(Long userId, StockRequest request);
    StockRecordDTO completeRecord(Long recordId);
    StockRecordDTO cancelRecord(Long recordId);
    Page<StockRecordDTO> getStockRecordsByUser(Long userId, Pageable pageable);
    List<StockRecordDTO> getStockRecordsByInventory(Long inventoryId);
    Page<StockRecordDTO> getStockInRecords(Pageable pageable);
    Page<StockRecordDTO> getStockOutRecords(Pageable pageable);
    Page<StockRecordDTO> getAllStockRecords(Pageable pageable);
}