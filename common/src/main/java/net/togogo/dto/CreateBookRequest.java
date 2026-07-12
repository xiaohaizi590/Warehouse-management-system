package net.togogo.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

//这是请求的数据传输对象（DTO），用于创建图书的请求数据封装
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookRequest {

    @NotBlank(message = "书名不能为空")
    private String title;           // 书名
    @NotBlank(message = "作者不能为空")
    private String author;          // 作者
    private String isbn;            // ISBN编号
    private String publisher;       // 出版社
    private String category;        // 分类（如：科技、文学、历史）
    private String description;     // 简介
    private Integer stock;          // 库存数量,可以为空，默认为0
    private Integer available;      // 可借数量
    private LocalDateTime publishDate; // 出版日期
    // 其他字段可以根据需要添加
    //这个模块由管理员进行使用
}
