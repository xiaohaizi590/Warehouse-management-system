package net.togogo.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
//这是请求的数据传输对象（DTO），用于借阅图书的请求数据封装
public class BorrowRequest {
    @NotNull(message = "图书ID不能为空")
    private Long bookId; // 借阅的图书ID
    private Integer renewCount; // 续借次数,可以为空，默认为0
    @NotNull(message = "借阅天数不能为空")
    private Integer borrowDays; // 借阅天数
}
