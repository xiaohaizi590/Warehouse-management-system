package net.togogo.repository;

import net.togogo.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long>, JpaSpecificationExecutor<Inventory> {

    Page<Inventory> findByProductNameContaining(String productName, Pageable pageable);
    Page<Inventory> findByCategory(String category, Pageable pageable);
    Page<Inventory> findBySupplier(String supplier, Pageable pageable);

    Optional<Inventory> findByBarcode(String barcode);
    Optional<Inventory> findByProductCode(String productCode);

    boolean existsByBarcode(String barcode);
    boolean existsByProductCode(String productCode);

    Page<Inventory> findByQuantityLessThanEqual(Integer minStock, Pageable pageable);
}