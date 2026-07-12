package net.togogo.repository;

import net.togogo.entity.BorrowRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {
    List<BorrowRecord> findByUserIdOrderByBorrowTimeDesc(Long userId);
    Page<BorrowRecord> findByUserId(Long userId, Pageable pageable);
    List<BorrowRecord> findByBookIdOrderByBorrowTimeDesc(Long bookId);
    List<BorrowRecord> findByStatusOrderByBorrowTimeDesc(BorrowRecord.Borrowstatus status);
    List<BorrowRecord> findByStatusAndDueTimeBefore(BorrowRecord.Borrowstatus status, LocalDateTime time);
    Page<BorrowRecord> findByStatusAndDueTimeBefore(BorrowRecord.Borrowstatus status, LocalDateTime time, Pageable pageable);
    Long countByBookIdAndStatus(Long bookId, BorrowRecord.Borrowstatus status);
    boolean existsByBookIdAndUserIdAndStatus(Long bookId, Long userId, BorrowRecord.Borrowstatus status);
}