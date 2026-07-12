package net.togogo.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

//借阅记录实体类
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "t_borrow_record")
public class BorrowRecord {

    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @Column(name = "book_id", nullable = false)
    private Long bookId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "borrow_time", nullable = false)
    private java.time.LocalDateTime borrowTime;

    @Column(name= "due_time", nullable = false)
    private java.time.LocalDateTime dueTime;

    @Column(name = "return_time")
    private java.time.LocalDateTime returnTime;// 归还时间要与应还时间进行比较，若归还时间晚于应还时间，则说明逾期归还

    @Column(name = "renew_count", nullable = false)
    private Integer renewCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Borrowstatus status;// 借阅状态：BORROWED（借出）、RETURNED（已归还）、OVERDUE（逾期）

    public enum Borrowstatus {
        BORROWED,
        RETURNED,
        OVERDUE
    }

    //进行该有的判断
    @PrePersist
    protected void onCreate() {
        if (borrowTime == null) {
            borrowTime = java.time.LocalDateTime.now();
        }
        if (dueTime == null) {
            dueTime = borrowTime.plusDays(14); // 默认借阅期限为14天
        }
        if (renewCount == null) {
            renewCount = 0;
        }
        if (status == null) {
            status = Borrowstatus.BORROWED;
        }
    }




















}
