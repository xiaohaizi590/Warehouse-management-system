package net.togogo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.togogo.entity.BorrowRecord;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BorrowRecordDTO {

    private Long id;
    private Long bookId;
    private Long userId;
    private String bookTitle;
    private String bookAuthor;
    private String userName;
    private java.time.LocalDateTime borrowTime;// 借阅时间
    private java.time.LocalDateTime dueTime;// 应还时间
    private java.time.LocalDateTime returnTime;// 归还时间要与应还时间进行比较，若归还时间晚于应还时间，则说明逾期归还
    private Integer renewCount;
    private BorrowRecord.Borrowstatus status;
    private Long overdueDays; // 逾期天数
}
