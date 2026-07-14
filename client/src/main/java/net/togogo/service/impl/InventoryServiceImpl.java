package net.togogo.service.impl;

import lombok.RequiredArgsConstructor;
import net.togogo.common.BusinessException;
import net.togogo.common.ResultCode;
import net.togogo.dto.InventoryDTO;
import net.togogo.dto.StockRecordDTO;
import net.togogo.dto.CreateInventoryRequest;
import net.togogo.dto.StockRequest;
import net.togogo.entity.Inventory;
import net.togogo.entity.StockRecord;
import net.togogo.entity.User;
import net.togogo.repository.InventoryRepository;
import net.togogo.repository.StockRecordRepository;
import net.togogo.repository.UserRepository;
import net.togogo.service.InventoryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockRecordRepository stockRecordRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public InventoryDTO createInventory(CreateInventoryRequest request) {
        if (request.getBarcode() != null && inventoryRepository.existsByBarcode(request.getBarcode())) {
            throw new BusinessException(ResultCode.BOOK_ISBN_EXIST);
        }
        if (request.getProductCode() != null && inventoryRepository.existsByProductCode(request.getProductCode())) {
            throw new BusinessException(ResultCode.BOOK_ISBN_EXIST);
        }

        Inventory inventory = Inventory.builder()
                .productName(request.getProductName())
                .productCode(request.getProductCode())
                .barcode(request.getBarcode())
                .supplier(request.getSupplier())
                .category(request.getCategory())
                .description(request.getDescription())
                .quantity(request.getQuantity() != null ? request.getQuantity() : 0)
                .unitPrice(request.getUnitPrice() != null ? request.getUnitPrice() : 0.0)
                .minStock(request.getMinStock() != null ? request.getMinStock() : 10)
                .location(request.getLocation())
                .build();

        Inventory saved = inventoryRepository.save(inventory);
        return convertToInventoryDTO(saved);
    }

    @Override
    public InventoryDTO getInventoryById(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToInventoryDTO(inventory);
    }

    @Override
    public Page<InventoryDTO> getAllInventories(Pageable pageable) {
        return inventoryRepository.findAll(pageable).map(this::convertToInventoryDTO);
    }

    @Override
    public Page<InventoryDTO> searchByProductName(String productName, Pageable pageable) {
        return inventoryRepository.findByProductNameContaining(productName, pageable).map(this::convertToInventoryDTO);
    }

    @Override
    public Page<InventoryDTO> searchByCategory(String category, Pageable pageable) {
        return inventoryRepository.findByCategory(category, pageable).map(this::convertToInventoryDTO);
    }

    @Override
    public Page<InventoryDTO> searchBySupplier(String supplier, Pageable pageable) {
        return inventoryRepository.findBySupplier(supplier, pageable).map(this::convertToInventoryDTO);
    }

    @Override
    public Page<InventoryDTO> getLowStockItems(Integer threshold, Pageable pageable) {
        return inventoryRepository.findByQuantityLessThanEqual(threshold, pageable).map(this::convertToInventoryDTO);
    }

    @Override
    @Transactional
    public InventoryDTO updateInventory(Long id, CreateInventoryRequest request) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        inventory.setProductName(request.getProductName());
        inventory.setProductCode(request.getProductCode());
        inventory.setBarcode(request.getBarcode());
        inventory.setSupplier(request.getSupplier());
        inventory.setCategory(request.getCategory());
        inventory.setDescription(request.getDescription());
        inventory.setQuantity(request.getQuantity());
        inventory.setUnitPrice(request.getUnitPrice());
        inventory.setMinStock(request.getMinStock());
        inventory.setLocation(request.getLocation());

        Inventory updated = inventoryRepository.save(inventory);
        return convertToInventoryDTO(updated);
    }

    @Override
    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = inventoryRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        Long pendingCount = stockRecordRepository.countByInventoryIdAndStatus(id, StockRecord.StockStatus.PENDING);
        if (pendingCount > 0) {
            throw new BusinessException(ResultCode.BOOK_BORROWED_CANNOT_DELETE);
        }

        inventoryRepository.delete(inventory);
    }

    @Override
    @Transactional
    public StockRecordDTO stockIn(Long userId, StockRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        double unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : inventory.getUnitPrice();
        double totalAmount = unitPrice * request.getQuantity();

        inventory.setQuantity(inventory.getQuantity() + request.getQuantity());
        inventory.setUnitPrice((inventory.getUnitPrice() * (inventory.getQuantity() - request.getQuantity()) + totalAmount) / inventory.getQuantity());
        inventoryRepository.save(inventory);

        StockRecord record = StockRecord.builder()
                .inventoryId(request.getInventoryId())
                .userId(userId)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .remark(request.getRemark())
                .stockType(StockRecord.StockType.IN)
                .status(StockRecord.StockStatus.COMPLETED)
                .build();

        StockRecord saved = stockRecordRepository.save(record);
        return convertToStockRecordDTO(saved, inventory.getProductName(), inventory.getProductCode());
    }

    @Override
    @Transactional
    public StockRecordDTO stockOut(Long userId, StockRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (inventory.getQuantity() < request.getQuantity()) {
            throw new BusinessException(ResultCode.BOOK_NOT_AVAILABLE);
        }

        double unitPrice = request.getUnitPrice() != null ? request.getUnitPrice() : inventory.getUnitPrice();
        double totalAmount = unitPrice * request.getQuantity();

        inventory.setQuantity(inventory.getQuantity() - request.getQuantity());
        inventoryRepository.save(inventory);

        StockRecord record = StockRecord.builder()
                .inventoryId(request.getInventoryId())
                .userId(userId)
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .totalAmount(totalAmount)
                .remark(request.getRemark())
                .stockType(StockRecord.StockType.OUT)
                .status(StockRecord.StockStatus.COMPLETED)
                .build();

        StockRecord saved = stockRecordRepository.save(record);
        return convertToStockRecordDTO(saved, inventory.getProductName(), inventory.getProductCode());
    }

    @Override
    @Transactional
    public StockRecordDTO completeRecord(Long recordId) {
        StockRecord record = stockRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (record.getStatus() != StockRecord.StockStatus.PENDING) {
            throw new BusinessException(ResultCode.RECORD_NOT_BORROWED);
        }

        Inventory inventory = inventoryRepository.findById(record.getInventoryId())
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (record.getStockType() == StockRecord.StockType.IN) {
            inventory.setQuantity(inventory.getQuantity() + record.getQuantity());
        } else {
            if (inventory.getQuantity() < record.getQuantity()) {
                throw new BusinessException(ResultCode.BOOK_NOT_AVAILABLE);
            }
            inventory.setQuantity(inventory.getQuantity() - record.getQuantity());
        }
        inventoryRepository.save(inventory);

        record.setStatus(StockRecord.StockStatus.COMPLETED);
        StockRecord saved = stockRecordRepository.save(record);
        return convertToStockRecordDTO(saved, inventory.getProductName(), inventory.getProductCode());
    }

    @Override
    @Transactional
    public StockRecordDTO cancelRecord(Long recordId) {
        StockRecord record = stockRecordRepository.findById(recordId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (record.getStatus() != StockRecord.StockStatus.PENDING) {
            throw new BusinessException(ResultCode.RECORD_NOT_BORROWED);
        }

        record.setStatus(StockRecord.StockStatus.CANCELLED);
        StockRecord saved = stockRecordRepository.save(record);

        Inventory inventory = inventoryRepository.findById(record.getInventoryId()).orElse(null);
        String productName = inventory != null ? inventory.getProductName() : "未知";
        String productCode = inventory != null ? inventory.getProductCode() : "未知";
        return convertToStockRecordDTO(saved, productName, productCode);
    }

    @Override
    public Page<StockRecordDTO> getStockRecordsByUser(Long userId, Pageable pageable) {
        return stockRecordRepository.findByUserId(userId, pageable)
                .map(record -> {
                    Inventory inventory = inventoryRepository.findById(record.getInventoryId()).orElse(null);
                    String productName = inventory != null ? inventory.getProductName() : "未知";
                    String productCode = inventory != null ? inventory.getProductCode() : "未知";
                    return convertToStockRecordDTO(record, productName, productCode);
                });
    }

    @Override
    public List<StockRecordDTO> getStockRecordsByInventory(Long inventoryId) {
        Inventory inventory = inventoryRepository.findById(inventoryId)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        return stockRecordRepository.findByInventoryIdOrderByRecordTimeDesc(inventoryId).stream()
                .map(record -> {
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToStockRecordDTO(record, inventory.getProductName(), inventory.getProductCode(), username);
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<StockRecordDTO> getStockInRecords(Pageable pageable) {
        return stockRecordRepository.findByStockType(StockRecord.StockType.IN, pageable)
                .map(record -> {
                    Inventory inventory = inventoryRepository.findById(record.getInventoryId()).orElse(null);
                    String productName = inventory != null ? inventory.getProductName() : "未知";
                    String productCode = inventory != null ? inventory.getProductCode() : "未知";
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToStockRecordDTO(record, productName, productCode, username);
                });
    }

    @Override
    public Page<StockRecordDTO> getStockOutRecords(Pageable pageable) {
        return stockRecordRepository.findByStockType(StockRecord.StockType.OUT, pageable)
                .map(record -> {
                    Inventory inventory = inventoryRepository.findById(record.getInventoryId()).orElse(null);
                    String productName = inventory != null ? inventory.getProductName() : "未知";
                    String productCode = inventory != null ? inventory.getProductCode() : "未知";
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToStockRecordDTO(record, productName, productCode, username);
                });
    }

    @Override
    public Page<StockRecordDTO> getAllStockRecords(Pageable pageable) {
        return stockRecordRepository.findAll(pageable)
                .map(record -> {
                    Inventory inventory = inventoryRepository.findById(record.getInventoryId()).orElse(null);
                    String productName = inventory != null ? inventory.getProductName() : "未知";
                    String productCode = inventory != null ? inventory.getProductCode() : "未知";
                    User user = userRepository.findById(record.getUserId()).orElse(null);
                    String username = user != null ? user.getUsername() : "未知";
                    return convertToStockRecordDTO(record, productName, productCode, username);
                });
    }

    private InventoryDTO convertToInventoryDTO(Inventory inventory) {
        return InventoryDTO.builder()
                .id(inventory.getId())
                .productName(inventory.getProductName())
                .productCode(inventory.getProductCode())
                .barcode(inventory.getBarcode())
                .supplier(inventory.getSupplier())
                .category(inventory.getCategory())
                .description(inventory.getDescription())
                .quantity(inventory.getQuantity())
                .unitPrice(inventory.getUnitPrice())
                .minStock(inventory.getMinStock())
                .location(inventory.getLocation())
                .createTime(inventory.getCreateTime())
                .build();
    }

    private StockRecordDTO convertToStockRecordDTO(StockRecord record, String productName, String productCode) {
        User user = userRepository.findById(record.getUserId()).orElse(null);
        String username = user != null ? user.getUsername() : "未知";
        return convertToStockRecordDTO(record, productName, productCode, username);
    }

    private StockRecordDTO convertToStockRecordDTO(StockRecord record, String productName,
                                                   String productCode, String username) {
        return StockRecordDTO.builder()
                .id(record.getId())
                .inventoryId(record.getInventoryId())
                .productName(productName)
                .productCode(productCode)
                .userId(record.getUserId())
                .userName(username)
                .recordTime(record.getRecordTime())
                .quantity(record.getQuantity())
                .unitPrice(record.getUnitPrice())
                .totalAmount(record.getTotalAmount())
                .remark(record.getRemark())
                .stockType(record.getStockType())
                .status(record.getStatus())
                .build();
    }
}